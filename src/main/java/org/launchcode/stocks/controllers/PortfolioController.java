package org.launchcode.stocks.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.launchcode.stocks.models.Stock;
import org.launchcode.stocks.models.StockHolding;
import org.launchcode.stocks.models.StockLookupException;
import org.launchcode.stocks.models.dao.StockHoldingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class PortfolioController extends AbstractController {

    @Autowired
    StockHoldingDao stockHoldingDao;
    
    @RequestMapping(value = "/portfolio")
    public String portfolio(HttpServletRequest request, Model model){

        Integer userId = (Integer) request.getSession().getAttribute(AbstractController.userSessionKey);
        
        // Implement portfolio display
    	List<StockHolding> shHoldings = stockHoldingDao.findByOwnerId(userId);
    	List<ArrayList<String>> portfolio = new ArrayList<ArrayList<String>>();
    	float portfolioValue = 0;

    	for (StockHolding holding : shHoldings) {
    		try {
    	    	ArrayList<String> holdings = new ArrayList<String>();
				Stock stock = Stock.lookupStock(holding.getSymbol());
				holdings.add(stock.getName());
				holdings.add(holding.getSymbol());
				holdings.add(Integer.toString(holding.getSharesOwned()));
				holdings.add(Float.toString(stock.getPrice()));
				float holdingValue = (float) holding.getSharesOwned() * stock.getPrice();
				holdings.add(Float.toString(holdingValue));
				portfolio.add(holdings);
				portfolioValue+=holdingValue;
			} catch (StockLookupException e) {
				e.printStackTrace();
			}
    	}
    	
    	model.addAttribute("portfolioValue", portfolioValue);
    	model.addAttribute("portfolio", portfolio);
        model.addAttribute("title", "Portfolio");
        model.addAttribute("portfolioNavClass", "active");

        return "portfolio";
    }

}
