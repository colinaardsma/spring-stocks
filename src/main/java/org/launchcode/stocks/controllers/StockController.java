package org.launchcode.stocks.controllers;

import javax.servlet.http.HttpServletRequest;

import org.launchcode.stocks.models.Stock;
import org.launchcode.stocks.models.StockHolding;
import org.launchcode.stocks.models.StockLookupException;
import org.launchcode.stocks.models.User;
import org.launchcode.stocks.models.dao.StockHoldingDao;
import org.launchcode.stocks.models.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class StockController extends AbstractController {

    @Autowired
    StockHoldingDao stockHoldingDao;
    
    @Autowired
    UserDao userDao;

    @RequestMapping(value = "/quote", method = RequestMethod.GET)
    public String quoteForm(Model model) {

        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");
        return "quote_form";
    }

    @RequestMapping(value = "/quote", method = RequestMethod.POST)
    public String quote(String symbol, Model model) {

        // Implement quote lookup
    	String error;
    	Stock stock;
		try {
			stock = Stock.lookupStock(symbol);
		} catch (StockLookupException e) {
			e.printStackTrace();
			error = "Invalid symbol";
	    	model.addAttribute("error", error);
			return "quote_form";
		}
    	
    	float stock_price = stock.getPrice();
    	String stock_desc = stock.getName();
    	
    	model.addAttribute("stock_desc", stock_desc);
    	model.addAttribute("stock_price", stock_price);

        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");

        return "quote_display";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.GET)
    public String buyForm(Model model) {

        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(String symbol, int numberOfShares, HttpServletRequest request, Model model) {
  
        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
    	
        Integer userId = (Integer) request.getSession().getAttribute(AbstractController.userSessionKey);
        User user = userDao.findByUid(userId);
        
        String error;
		Stock stock;

		// Implement buy action
		try {
			stock = Stock.lookupStock(symbol);
			
	    	// see if user has enough cash to purchase
			float stock_price = stock.getPrice();    	
	    	float userCash = user.getCash();
	    	float transactionCost = stock_price * numberOfShares;
	    	
	    	if (transactionCost > userCash) {
	    		error = "Not enough cash for purchase";
	            model.addAttribute("error", error);
		        return "transaction_form";
	    	}
	        
			StockHolding holding = StockHolding.buyShares(user, symbol, numberOfShares);
			stockHoldingDao.save(holding);
		} catch (StockLookupException e) {
			e.printStackTrace();
			error = "Invalid symbol";
	    	model.addAttribute("error", error);
	        return "transaction_form";
		}
        
        return "transaction_confirm";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    public String sellForm(Model model) {
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.POST)
    public String sell(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");

        Integer userId = (Integer) request.getSession().getAttribute(AbstractController.userSessionKey);
        User user = userDao.findByUid(userId);
        
        String error;
        
        // Implement sell action
        try {
            StockHolding h = stockHoldingDao.findBySymbolAndOwnerId(symbol, userId);
            
            // check if shares owned
        	int sharesOwned = h.getSharesOwned();
            if (numberOfShares > sharesOwned) {
        		error = "Not enough shares owned";
                model.addAttribute("error", error);
    	        return "transaction_form";
            }
            
			StockHolding holding = StockHolding.sellShares(user, h.getSymbol(), numberOfShares);
			stockHoldingDao.save(holding);
		} catch (StockLookupException e) {
			e.printStackTrace();
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			error = "Invalid symbol or stock not owned";
            model.addAttribute("error", error);
			return "transaction_form";
		}
        
        return "transaction_confirm";
    }

}
