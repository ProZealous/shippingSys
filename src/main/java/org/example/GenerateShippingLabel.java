//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.swing.JOptionPane;

public class GenerateShippingLabel {
    private String apikey = "aeJ0wnl4yhdjrToxyjIPTpwSHkp4viDd";
    public GenerateShippingLabel() {}
    public void generateLabel(Order order) {
        if (order.getShippingID() == null) {
            String url = "https://api2.postnord.com/rest/shipment/v3/edi/labels/pdf?apikey=" + this.apikey + "&paperSize=LABEL&rotate=0&multiPDF=false&pnInfoText=false&labelsPerPage=100&page=1&processOffline=false";
            String jsonRequest = String.format("""
                        {
                            "messageDate": "2020-11-26T13:39:59.9125844+00:00",
                            "messageFunction": "Instruction",
                            "messageId": "20201126_2",
                            "application": {
                                "applicationId": 1438,
                                "name": "PostNord",
                                "version": "1.0"
                            },
                            "updateIndicator": "Original",
                            "shipment": [
                                {
                                    "shipmentIdentification": {
                                        "shipmentId": "0"
                                    },
                                    "dateAndTimes": {
                                        "loadingDate": "%s"
                                    },
                                    "service": {
                                        "basicServiceCode": "11",
                                        "additionalServiceCode": ["C5"]
                                    },
                                    "freeText": [],
                                    "numberOfPackages": {
                                        "value": 1
                                    },
                                    "totalGrossWeight": {
                                        "value": %s,
                                        "unit": "KGM"
                                    },
                                    "parties": {
                                        "consignor": {
                                            "issuerCode": "Z12",
                                            "partyIdentification": {
                                                "partyId": "20880207",
                                                "partyIdType": "160"
                                            },
                                            "party": {
                                                "nameIdentification": {
                                                    "name": "TeddyWear"
                                                },
                                                "address": {
                                                    "streets": ["Neptunusgatan 11"],
                                                    "postalCode": "25473",
                                                    "city": "Helsingborg",
                                                    "countryCode": "SE"
                                                }
                                            }
                                        },
                                        "consignee": {
                                            "party": {
                                                "nameIdentification": {
                                                    "name": "%s %s"
                                                },
                                                "address": {
                                                    "streets": ["%s"],
                                                    "postalCode": "%s",
                                                    "city": "%s",
                                                    "countryCode": "SE"
                                                },
                                                "contact": {
                                                    "contactName": "%s %s",
                                                    "emailAddress": "%s",
                                                    "smsNo": "%s"
                                                }
                                            }
                                        },
                                        "deliveryParty": {
                                            "partyIdentification": {
                                                "partyId": "9814",
                                                "partyIdType": "156"
                                            },
                                            "party": {
                                                "nameIdentification": {
                                                    "name": "COLLECTSHOP SPAR"
                                                },
                                                "address": {
                                                    "streets": ["Nordens Alle 14"],
                                                    "postalCode": "9800",
                                                    "city": "Hjørring",
                                                    "countryCode": "DK"
                                                }
                                            }
                                        }
                                    },
                                    "goodsItem": [
                                        {
                                            "packageTypeCode": "PC",
                                            "items": [
                                                {
                                                    "itemIdentification": {
                                                        "itemId": "0",
                                                        "itemIdType": "SSCC"
                                                    },
                                                    "grossWeight": {
                                                        "value": %s,
                                                        "unit": "KGM"
                                                    }
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                        """,
                    getCurrentDate(), // For loadingDate
                    order.getWeight(), // Weight
                    order.getFirstName(),
                    order.getLastName(),
                    order.getStreet1(),
                    order.getZipcode(),
                    order.getCity(),
                    order.getFirstName(),
                    order.getLastName(),
                    order.getEmail(),
                    order.getPhone(),
                    order.getWeight() // Weight again
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(BodyPublishers.ofString(jsonRequest)).build();
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());


                String jsonResponse = response.body();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                String bookingId = rootNode.path("bookingResponse").path("idInformation").get(0).path("ids").get(0).path("value").asText();
                String trackingUrl = rootNode.path("bookingResponse").path("idInformation").get(0).path("urls").get(0).path("url").asText();
                order.setShippingID(bookingId);
                order.setShippingUrl(trackingUrl);
                String base64EncodedLabel = this.extractLabelFromResponse(jsonResponse);
                base64EncodedLabel = base64EncodedLabel.replace('-', '+').replace('_', '/');
                byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedLabel);
                this.savePdf(decodedBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog((Component)null, "This order already has a shipping ID: " + order.getShippingID());
        }

    }

    public void getShipmentLabelsPDF(String shipmentId) {
        System.out.println("1");
        HttpClient client = HttpClient.newHttpClient();

        String requestBody = String.format("[{\"id\": \"%s\"}]", shipmentId);
        String url = String.format("https://api2.postnord.com/rest/shipment/v3/labels/ids/pdf?apikey=%s&paperSize=LABEL&rotate=0&multiPDF=false&pnInfoText=false&labelsPerPage=100&page=1&processOffline=false", apikey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String jsonResponse = response.body();

            String base64EncodedLabel = extractLabelFromResponse(jsonResponse);
            System.out.println(base64EncodedLabel);
            base64EncodedLabel = base64EncodedLabel.replace('-', '+').replace('_', '/');

            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedLabel);
            savePdf(decodedBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String extractLabelFromResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode labelNode = rootNode.path(0).path("printout").path("data");
        return labelNode.asText();
    }

    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }

    private void savePdf(byte[] data) {
        try {
            Path path = Paths.get("shipping_label.pdf");
            Files.write(path, data);

            if (Desktop.isDesktopSupported()) {
                File file = path.toFile();
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Desktop is not supported. Cannot open the file automatically.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
