package com.stock.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.histquotes2.HistoricalDividend;
import yahoofinance.histquotes2.HistoricalSplit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Controller
public class MainController{

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) throws IOException {

        model.addAttribute("eventName", "FIFA 2018");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        // from 5 years ago
        from.add(Calendar.YEAR, -1);
        Stock stock = null;
        try {
            stock = YahooFinance.get("TSLA", from, to, Interval.MONTHLY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String namek = stock.getName();
        model.addAttribute("name", namek);
        BigDecimal price = stock.getQuote().getPrice();
        model.addAttribute("price", price);
        Long shares = stock.getStats().getSharesOutstanding();
        model.addAttribute("shares", shares);
        BigDecimal sale = stock.getStats().getPriceSales();
        model.addAttribute("sale", sale);
        BigDecimal totalRevenue = stock.getStats().getRevenue();
        model.addAttribute("totalRevenue", totalRevenue);
        BigDecimal peg = stock.getStats().getPeg();
        model.addAttribute("peg", peg);
        BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();
        model.addAttribute("dividend", dividend);
        List<HistoricalQuote> sstats = stock.getHistory(from, to, Interval.MONTHLY);
        model.addAttribute("stats", sstats);

        final IEXTradingClient iexTradingClient = IEXTradingClient.create();
        final Quote quote = iexTradingClient.executeRequest(new QuoteRequestBuilder()
                .withSymbol("AAPL")
                .build());
        System.out.println(quote);

        stock.print();
        return "index";
    }
}