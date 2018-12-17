package com.stock.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes2.HistoricalDividend;
import yahoofinance.histquotes2.HistoricalSplit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) throws IOException {
        // this attribute will be available in the view index.html as a thymeleaf variable
        model.addAttribute("eventName", "FIFA 2018");
        // this just means render index.html from static/ area

        Stock stock = null;
        try {
            stock = YahooFinance.get("INTC");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String namek = stock.getName();
        model.addAttribute("name", namek);
//        model.addAttribute("name", "Marmeladka");
//        System.out.println("Man or me " + namek);
        BigDecimal price = stock.getQuote().getPrice();
        model.addAttribute("price", price);
        BigDecimal change = stock.getQuote().getChangeInPercent();
        model.addAttribute("change", change);
        BigDecimal peg = stock.getStats().getPeg();
        model.addAttribute("peg", peg);
        BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();
        model.addAttribute("dividend", dividend);
        List<HistoricalDividend> sstats = stock.getDividendHistory();
        model.addAttribute("stats", sstats);

        stock.print();
        return "index";
    }
}