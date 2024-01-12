package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Controller {
    private JFrame frame;
    private JList<Order> orderList;
    private JTextArea orderDetailsArea;
    private JButton createLabelButton;
    private JButton getLabel;
    private JButton distribute;
    private JButton refreshButton;

    private DefaultListModel<Order> orderListModel;
    private Order selectedOrderToPrint;

    GenerateShippingLabel generator;

    List<Order> orderDetails = new ArrayList<>();

    public Controller() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Order Management System");
        frame.setIconImage(new ImageIcon("path_to_custom_icon.png").getImage());

        generator = new GenerateShippingLabel();


        // Modern UI colors and fonts
        Color backgroundColor = new Color(245, 245, 245);
        Color buttonColor = new Color(76, 175, 80);
        Color textColor = Color.DARK_GRAY;

        Font textFont = new Font("SansSerif", Font.PLAIN, 14);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);

        // Styling the list
        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setFont(textFont);
        orderList.setBackground(backgroundColor);
        orderList.setForeground(textColor);
        orderList.setSelectionBackground(buttonColor);
        orderList.setSelectionForeground(Color.WHITE);

        // Styling the text area
        orderDetailsArea = new JTextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(textFont);
        orderDetailsArea.setBackground(backgroundColor);
        orderDetailsArea.setForeground(textColor);
        orderDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refreshButton = new JButton("Refresh Orders");
        styleButton(refreshButton, buttonColor, textColor, buttonFont);

        // Adding the Refresh button action
        refreshButton.requestFocusInWindow();
        refreshButton.addActionListener(e -> refreshOrders());

        // Panel for buttons


        // Styling the buttons
        createLabelButton = new JButton("Create Shipping Label");
        createLabelButton.requestFocusInWindow();
        styleButton(createLabelButton, buttonColor, Color.DARK_GRAY, buttonFont);

        getLabel = new JButton("Get Shipping Label");
        styleButton(getLabel, buttonColor, Color.DARK_GRAY, buttonFont);

        distribute = new JButton("Distribute Order");
        styleButton(distribute, buttonColor, Color.DARK_GRAY, buttonFont);

        // Adding components to the frame using BorderLayout
        frame.setLayout(new BorderLayout(10, 10));
        frame.add(new JScrollPane(orderList), BorderLayout.WEST);
        frame.add(new JScrollPane(orderDetailsArea), BorderLayout.CENTER);


        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(createLabelButton);
        buttonPanel.add(getLabel);
        buttonPanel.add(distribute);
        buttonPanel.add(refreshButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Set the window to be visible and the default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setVisible(true);

        // Center the window
        frame.setLocationRelativeTo(null);

        orderList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                Order selectedOrder = orderList.getSelectedValue();
                if (selectedOrder != null) {
                    displayOrderDetails(selectedOrder);
                    selectedOrderToPrint = selectedOrder;
                }
            }
        });

        getLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String shippingId = selectedOrderToPrint.getShippingID();
                generator.getShipmentLabelsPDF("00573132900021782794");
            }
        });

        createLabelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!selectedOrderToPrint.getCancelled().equals("null")) {
                    JOptionPane.showMessageDialog(null, "You can't create a label for a canceled order.");
                } else if (selectedOrderToPrint.getShippingID() != null) {
                    JOptionPane.showMessageDialog(null, "You can't create a new label for this order. The shipment ID is: " + selectedOrderToPrint.getShippingID());
                } else {
                    generator.generateLabel(selectedOrderToPrint);
                }

            }
        });

        distribute.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String shippingId = selectedOrderToPrint.getShippingID();
                if (shippingId != null) {

                } else {
                    JOptionPane.showMessageDialog(null, "You need to book shipping before distributing the order.");

                }
            }
        });



        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private void refreshOrders() {
        orderListModel.clear();
        orderList.clearSelection();
        createOrders();
    }


    private void displayOrderDetails(Order order) {
        String details = "Order Number: " + order.getOrderNumber() +
                "\nNamn: " + order.getFirstName() + " " + order.getLastName() +
                "\nEmail: " + order.getEmail() +
                "\nMobilnummer: " + order.getPhone() +
                "\nPris: " + order.getTotalPrice() + " kr" +
                "\nVikt: " + order.getWeight() + " kg" +
                "\nGata 1: " + order.getStreet1() +
                "\nGata 2: " + order.getStreet2() +
                "\nPostnummer: " + order.getZipcode() +
                "\nStad: " + order.getCity() +
                "\nLand: " + order.getCountry() +
                "\nTracking Number: " + order.getShippingID() +
                "\nTracking URL: " + order.getShippingUrl() +
                "\nShipment Company: " + order.getTrackingCompany() +
                "";
        orderDetailsArea.setText(details);
    }

    public void createOrders() {
        String url = "https://0387e5-2.myshopify.com/admin/api/2023-10/orders.json?status=any";
        String apiKey = "3c3c601217c6bc498edcd7ff77296e8f";
        String apiPassword = "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";
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((apiKey + ":" + apiPassword).getBytes());
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", authHeader)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String jsonResponse = response.body();
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    processOrders(rootNode);
                    break; // Break the loop if successful
                } else {
                    System.out.println("Failed to fetch orders, retrying... Attempt: " + (i + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (i == maxRetries - 1) {
                    System.out.println("Failed to fetch orders after multiple attempts.");
                }
            }
        }
    }

    private void processOrders(JsonNode rootNode) {
        JsonNode ordersNode = rootNode.path("orders");

        for (JsonNode order : ordersNode) {
            String orderId = order.path("id").asText();
            String fullfilled = order.path("fulfillment_status").asText();
            String cancelled = order.path("cancelled_at").asText();
            String orderNumber = order.path("order_number").asText();
            String totalPrice = order.path("total_price").asText();
            String firstName = order.path("billing_address").path("first_name").asText();
            String lastName = order.path("billing_address").path("last_name").asText();
            String street1 = order.path("shipping_address").path("address1").asText();
            String street2 = order.path("shipping_address").path("address2").asText();
            String zipcode = order.path("shipping_address").path("zip").asText();
            String city = order.path("shipping_address").path("city").asText();
            String phone = order.path("billing_address").path("phone").asText();
            String email = order.path("contact_email").asText();
            String country = order.path("shipping_address").path("country").asText();
            String weight = order.path("total_weight").asText();

            String trackingNumber = null;
            String trackingCompany = "";
            String trackingURL = "";
            JsonNode fulfillments = order.path("fulfillments");
            for (JsonNode fulfillment : fulfillments) {
                trackingNumber = fulfillment.path("tracking_number").asText().replace(" ", "");
                trackingURL = fulfillment.path("tracking_url").asText();
                trackingCompany = fulfillment.path("tracking_company").asText();
            }

            Order orderObj = new Order(orderId, orderNumber, totalPrice, firstName, lastName, street1, street2, zipcode, city, phone, email, country,weight, fullfilled, cancelled, trackingNumber, trackingURL, trackingCompany);
            orderDetails.add(orderObj);
        }

        for (Order order : orderDetails) {
            orderListModel.addElement(order);

        }
    }

    private void styleButton(JButton button, Color bgColor, Color textColor, Font font) {
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
}