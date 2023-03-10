package constants;

import util.Currencies;

public final class Constants {
    public static String CURRENCY = Currencies.USA.getCurrency();
    public static int CASHIER_NUMBER = 0;
    public static double TOTAL_SUM = 0;
    public static final int DISCOUNT_AFTER = 5;
    public static double CARD_DISCOUNT;
    public static double STANDARD_DISCOUNT = 0;
    public static double PRODUCT_DISCOUNT;
    public static final String OUTPUT_LINE = "_______________________________________\n";
    public static String RECEIPT_MESSAGE = "Для вывода чека введите номер и количество товара, а также номер дисконтной карты";
    public static String READ_MESSAGE = "Для чтения чека введите путь к файлу";
    public static String EXIT_MESSAGE = "Для выхода из программы введите 'exit'";
    public static String MENU_MESSAGE = String.format("%nRECEIPT BUILDER: %n%s%n%s%n%s%n: ", RECEIPT_MESSAGE, READ_MESSAGE, EXIT_MESSAGE);
    public static String PRODUCTS_SELECT = """
            SELECT * FROM company.product WHERE id = ?
            """;
    public static String CARD_SELECT = """
            SELECT * FROM company.discount_card WHERE id = ?
            """;
    public static String CREATE_TABLES = """
            CREATE SCHEMA IF NOT EXISTS company;
            CREATE TABLE IF NOT EXISTS company.product
            (
                id       SERIAL PRIMARY KEY,
                name     VARCHAR(32) UNIQUE NOT NULL,
                quantity INT,
                price    DECIMAL            NOT NULL,
                discount boolean
            );
            CREATE TABLE IF NOT EXISTS company.discount_card
            (
                id       INT UNIQUE         NOT NULL,
                discount DECIMAL            NOT NULL
            );
            INSERT INTO company.product (name, price, discount) 
            values ('Apple', 1.19, false),
                   ('Banana', 2.49, false),
                   ('Fish', 5.99, true),
                   ('Cheese', 7.29, true),
                   ('Chocolate', 8.29, true),
                   ('Beef Steak', 17.99, true),
                   ('Chocolate Milk', 4.99, true)
                    ON CONFLICT DO NOTHING;

            INSERT INTO company.discount_card (id, discount)
            VALUES (1111, 0.1),
                   (2222, 0.2),
                   (3333, 0.3),
                   (4444, 4)
                    ON CONFLICT DO NOTHING;
                        """;
}
