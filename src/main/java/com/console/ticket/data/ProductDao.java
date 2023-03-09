package com.console.ticket.data;

import com.console.ticket.entity.Product;
import com.console.ticket.exception.DatabaseException;
import com.console.ticket.exception.InputException;
import com.console.ticket.util.ConnectionManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Setter
public class ProductDao {


    private static ProductDao INSTANCE;
    private static String PRODUCT_FIND = """
            SELECT * FROM company.product WHERE id = ?
            """;

    private static String PRODUCT_FIND_ALL = """
            SELECT * FROM company.product
            """;
    private static String PRODUCT_DELETE = """
            DELETE FROM company.product WHERE id = ?
            """;
    private static String PRODUCT_SAVE = """
            INSERT INTO company.product (name, quantity, price, discount) 
            VALUES (?, ?, ?, ?);
            """;
    private static String PRODUCT_UPDATE = """
            UPDATE company.product
            SET name = ?,
                quantity = ?,
                price = ?,
                discount = ?
            WHERE id = ?
            """;

    public Optional<Product> findById(Integer id) throws DatabaseException {
        if (id == null || id < 0) {
            throw new InputException("Error find product by id: " + id);
        }
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(PRODUCT_FIND)) {

            preparedStatement.setObject(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Product product = null;

            if (resultSet.next()) {
                product = buildProduct(resultSet);
            }

            return Optional.ofNullable(product);
        } catch (SQLException e) {
            throw new DatabaseException("Error find product by id: " + id, e);
        }
    }

    public void delete(Integer id) throws DatabaseException {
        if (id == null || id < 0) {
            throw new InputException("Error find product by id: " + id);
        }
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(PRODUCT_DELETE)) {
            preparedStatement.setObject(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error delete product by id: " + id, e);
        }
    }

    public Product save(Product product) throws DatabaseException {
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(PRODUCT_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setInt(2, product.getQuantity());
            preparedStatement.setDouble(3, product.getPrice());
            preparedStatement.setBoolean(4, product.isDiscount());

            preparedStatement.executeUpdate();
            ResultSet keys = preparedStatement.getGeneratedKeys();
            if(keys.next()) {
                product.setId(keys.getInt("id"));
            }

            return product;
        } catch (SQLException e) {
            throw new DatabaseException("Error save product: " + product.getName(), e);
        }
    }
    public void update (Product product) throws DatabaseException {
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(PRODUCT_UPDATE)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setInt(2, product.getQuantity());
            preparedStatement.setDouble(3, product.getPrice());
            preparedStatement.setBoolean(4, product.isDiscount());
            preparedStatement.setInt(5, product.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error update product: " + product.getName(), e);
        }
    }


    private Product buildProduct(ResultSet resultSet) throws DatabaseException {
        try {
            return Product.builder().id(resultSet.getInt("id"))
                    .name(resultSet.getString("name"))
                    .price(resultSet.getDouble("price"))
                    .isDiscount(resultSet.getBoolean("discount"))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Error create product: ", e);
        }
    }

    public List<Optional<Product>> findAll() throws DatabaseException {
        try (var connection = ConnectionManager.open();
             var preparedStatement = connection.prepareStatement(PRODUCT_FIND_ALL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            List<Optional<Product>> productsList = new ArrayList<>();

            while (resultSet.next()) {
                Optional<Product> product = Optional.ofNullable(Product.builder()
                        .id(resultSet.getInt("id"))
                        .name(resultSet.getString("name"))
                        .price(resultSet.getDouble("price"))
                        .isDiscount(resultSet.getBoolean("discount"))
                        .build());
                productsList.add(product);
            }
            return productsList;
        } catch (SQLException e) {
            throw new DatabaseException("Error get all cards from database: " + e.getMessage());
        }
    }

    public static ProductDao getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductDao();
        }
        return INSTANCE;
    }
}
