package com.stock.management.controllers;

//import com.stock.management.helperClasses.IEXRealtime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.zankowski.iextrading4j.api.stocks.*;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Controller
public class MainController{

    String corpName = "EA";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) throws IOException {

        model.addAttribute("eventName", "FIFA 2018");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        // from 5 years ago
        from.add(Calendar.YEAR, -1);
        Stock stock = null;
        try {
            stock = YahooFinance.get("AAPL", from, to, Interval.MONTHLY);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String namek = stock.getName();
//        model.addAttribute("name", namek);
//        BigDecimal price = stock.getQuote().getPrice();
//        model.addAttribute("price", price);
//        Long shares = stock.getStats().getSharesOutstanding();
//        model.addAttribute("shares", shares);
//        BigDecimal sale = stock.getStats().getPriceSales();
//        model.addAttribute("sale", sale);
//        BigDecimal totalRevenue = stock.getStats().getRevenue();
//        model.addAttribute("totalRevenue", totalRevenue);
//        BigDecimal peg = stock.getStats().getPeg();
//        model.addAttribute("peg", peg);
//        BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();
//        model.addAttribute("dividend", dividend);
//        List<HistoricalQuote> sstats = stock.getHistory(from, to, Interval.MONTHLY);
//        model.addAttribute("stats", sstats);

        final IEXTradingClient iexTradingClient = IEXTradingClient.create();
        final Quote quote = iexTradingClient.executeRequest(new QuoteRequestBuilder()
                .withSymbol(corpName)
                .build());

        final BatchStocks batchStocks = iexTradingClient.executeRequest(new BatchStocksRequestBuilder()
                .withSymbol(corpName)
                .addType(BatchStocksType.BOOK)
                .addType(BatchStocksType.FINANCIALS)
                .addType(BatchStocksType.COMPANY)
                .build());
        System.out.println(quote);


        final TodayIpos todayIpos = iexTradingClient.executeRequest(new TodayIposRequestBuilder()
                .build());
        System.out.println("IPOS" + todayIpos);

        final KeyStats keyStats = iexTradingClient.executeRequest(new KeyStatsRequestBuilder()
                .withSymbol(corpName)
                .build());

        String name = quote.getCompanyName();
        model.addAttribute("name", name);

        BigDecimal price = quote.getLatestPrice();
        model.addAttribute("price", price);
        System.out.println("Latest price: " + price);

        BigDecimal sharesOutstanding = keyStats.getSharesOutstanding();
        model.addAttribute("sharesOut", sharesOutstanding);
        System.out.println("Shares Outstanding: " + sharesOutstanding);

        BigDecimal ebitda = keyStats.getEBITDA();
        model.addAttribute("ebitda", ebitda);
        System.out.println("EBITDA: " + ebitda);
//        System.out.println("Sec rev (ebitda): " + keyStats.getRevenue());
//        System.out.println("Price to Book: " + keyStats.getPriceToBook());

        BigDecimal bookPerShare = keyStats.getRevenuePerShare();
        model.addAttribute("bookPerShare", bookPerShare);
        System.out.println("Revenue per share (book v per share): " + bookPerShare);

        BigDecimal marketCap = keyStats.getMarketcap();
        model.addAttribute("marketCap", marketCap);
        System.out.println("Market capital: " + marketCap);

        BigDecimal revenue = batchStocks.getFinancials().getFinancials().get(0).getTotalRevenue();
        model.addAttribute("revenue", revenue);
        System.out.println("Revenue: " + revenue);

        BigDecimal netIncome = batchStocks.getFinancials().getFinancials().get(0).getNetIncome();
        model.addAttribute("netIncome", netIncome);
        System.out.println("Net Income: " + netIncome);

        BigDecimal totalDebt = batchStocks.getFinancials().getFinancials().get(0).getTotalDebt();
        model.addAttribute("totalDebt", totalDebt);
        System.out.println("Total debt: " + totalDebt);

        BigDecimal totalCash = batchStocks.getFinancials().getFinancials().get(0).getTotalCash();
        model.addAttribute("totalCash", totalCash);
        System.out.println("Total cash: " + totalCash);

//        stock.print();
        return "index";
    }

//    @RequestMapping(value = "/iexm", method = RequestMethod.GET)
//    public String indexIEX(Model model) throws IOException {
//        String STOCKS_CSV = "MSFT,AAPL,GE";
//
//            JSONParser parser = new JSONParser();
//
//            IEXRealtime realtime = new IEXRealtime(
//                    "", // username
//                    "", // password
//                    false, // debug
//                    new IEXRealtime.TextMessageHandler()  // Message Handler interface Callback
//                    {
//                        @Override
//                        public void handleMessage(String msg) // handleMessage override
//                        {
//                            try {
//                                JSONObject json = (JSONObject) parser.parse(msg);
//                                String event = (String) json.get("event");
//                                switch (event) {
//                                    case "quote":
//                                        System.out.println("QUOTE: " + json.toJSONString());
//                                    default:
//                                        IEXRealtime.debug("Non-quote message:" + json.toJSONString());
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//
//            if (STOCKS_CSV != null) {
//                String[] stocksArray = STOCKS_CSV.split(",");
//                realtime.join(stocksArray);
//            }
//        return null;
//        }
    }
