package com.console.ticket.service;

import com.console.ticket.constants.Constants;
import com.console.ticket.entity.Card;
import com.console.ticket.entity.Company;
import com.console.ticket.entity.Product;
import com.console.ticket.exception.DatabaseException;
import com.console.ticket.exception.FileException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiptBuilder {
    private static String RECEIPT;

    public static String writeReceipt(Company company, Card foundCard, List<Product> foundProducts) {
        String headerInfo = buildHeaderInfo(company);
        String productInfo = buildProductInfo(foundProducts);
        String totalInfo = buildTotalInfo(foundCard, foundProducts);

        RECEIPT = headerInfo.concat(productInfo).concat(totalInfo);

        try {
            FileService.writeReceipt(RECEIPT);
        } catch (FileException e) {
            System.out.println("Exception write receipt to file: " + e.getMessage());
        }

        return RECEIPT;
    }

    public static void readReceipt(String path) {
        FileService.readReceipt(path);
    }

    private static String buildHeaderInfo(Company company) {
        Constants.CASHIER_NUMBER++;

        return new StringBuilder()
                .append(String.format("%s%25s%n%s", Constants.OUTPUT_LINE, "CASH RECEIPT", Constants.OUTPUT_LINE))
                .append(String.format("%26s%n", company.getName()))
                .append(String.format("%s%n%n", company.getAddress()))
                .append("CASHIER №:" + Constants.CASHIER_NUMBER)
                .append(String.format(("%18s%-17s%n"), "Date: ", DateTimeService.getCurrentDate()))
                .append(String.format(("%29s%-17s%n%s"), "Time: ", DateTimeService.getCurrentTime(), Constants.OUTPUT_LINE))
                .append("QTY:  ")
                .append("DESCRIPTION:")
                .append(String.format("%13s", "PRICE: "))
                .append(String.format("%7s%n", "TOTAL"))
                .toString();
    }

    private static String buildProductInfo(List<Product> foundProducts) throws DatabaseException {
        StringBuilder receiptBuilder = new StringBuilder();

        for (Product product : foundProducts) {
            if (product.getQuantity() > 0) {
                int quantity = product.getQuantity();
                double price = product.getPrice();

                receiptBuilder
                        .append(String.format("%-6d", quantity))
                        .append(String.format("%-18s", product.getName()))
                        .append(String.format("%s%-8.2f", Constants.CURRENCY, price))
                        .append(String.format("%s%-4.2f%n", Constants.CURRENCY, (price * quantity)));

                if (product.isDiscount() && product.getQuantity() > Constants.DISCOUNT_AFTER) {
                    double discountSize = ReceiptCalculator.calculateDiscountPerProduct(product);
                    receiptBuilder.append(String.format("%16s", "(discount)"))
                            .append(String.format("%18s%-6.2f%n", "-" + Constants.CURRENCY, discountSize))
                            .toString();
                }
            }
        }

        return receiptBuilder.toString();
    }

    private static String buildTotalInfo(Card foundCard, List<Product> foundProducts) {
        double totalSum = ReceiptCalculator.calculateDiscountPricePerCard(foundProducts, foundCard);
        double cardDiscount = ReceiptCalculator.calculateCardDiscount(foundProducts, foundCard);
        foundProducts.forEach(product -> product.setQuantity(0));

        return new StringBuilder()
                .append(Constants.OUTPUT_LINE + "\n")
                .append(String.format("BUYER ID: [%d]%n", foundCard.getCardNumber()))
                .append(String.format("discount: %24s%-6.2f%n", Constants.CURRENCY, cardDiscount))
                .append(String.format("TOTAL: %27s%-6.2f", Constants.CURRENCY, totalSum))
                .toString();
    }
}