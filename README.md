# ACES Bitcoin-Ark Lite Channel Service

ACES Bitcoin to Ark channel service using Bitcoin Electrum such that service providers
do not need to run a full bitcoin node to run this service.

https://electrumx.readthedocs.io/en/latest/protocol.html

## Set up local database

```
docker run -d -p 5432:5432 \
--name aces_btc_ark_lite_channel_service_db \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_USER=postgres \
-e POSTGRES_DB=aces_btc_ark_lite_channel_service_db \
postgres:9.6.1
```


### Configuration

Copy [/main/resources/application.yml](main/resources/application.yml) into an external file on your system
(for example: `/etc/{service-name}/application.yml`) and replace configuration properties to match your
local setup. For example, you would need to change the service address and passphrase to an actual account.


### Run Channel Service

```
mvn clean spring-boot:run --spring.config.location=file:/etc/{service-name}/application.yml
```

### Run Channel Service (production)


To run the application in a live environment, you can build a jar package using `mvn package` and then
run the jar app generated under `/target` build directory with you custom configuration:

```
java -jar {jar-name}.jar --spring.config.location=file:/etc/{service-name}/application.yml
```


## Using the Service

Get service info:

```
curl http://localhost:9190/
```
```
{
  "name" : "ACES BTC-ARK Lite Channel Service",
  "description" : "ACES BTC to ARK Channel service for transferring BTC to ARK",
  "version" : "1.0.0",
  "websiteUrl" : "https://arkaces.com",
  "instructions" : "After this contract is executed, any BTC sent to depositBtcAddress will be exchanged for ARK and  sent directly to the given recipientArkAddress less service fees.\n",
  "flatFee" : "0.0001",
  "flatFeeUnit": "BTC",
  "percentFee" : "1.00",
  "capacities": [{
    "value": "50.00",
    "unit": "ARK"
  }],
  "inputSchema" : {
    "type" : "object",
    "properties" : {
      "recipientArkAddress" : {
        "type" : "string"
      }
    },
    "required" : [ "recipientArkAddress" ]
  },
  "outputSchema" : {
    "type" : "object",
    "properties" : {
      "depositBtcAddress" : {
        "type" : "string"
      },
      "recipientArkAddress" : {
        "type" : "string"
      },
      "transfers" : {
        "type" : "array",
        "properties" : {
          "btcAmount" : {
            "type" : "string"
          },
          "btcToArkRate" : {
            "type" : "string"
          },
          "btcFlatFee" : {
            "type" : "string"
          },
          "btcPercentFee" : {
            "type" : "string"
          },
          "btcTotalFee" : {
            "type" : "string"
          },
          "arkSendAmount" : {
            "type" : "string"
          },
          "arkTransactionId" : {
            "type" : "string"
          },
          "createdAt" : {
            "type" : "string"
          }
        }
      }
    }
  }
}
```

Create a new Service Contract:

```
curl -X POST http://localhost:9190/contracts \
-H 'Content-type: application/json' \
-d '{
  "arguments": {
    "recipientArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx"
  }
}' 
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
  
    "recipientArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "depositBtcAddress": "mu7gjSBLssPhKYuYU4qqBGFzjbh7ZTA6uY",
    "transfers": []
}
```

Get Contract information after sending BTC funds to `depositBtcAddress`:

```
curl -X GET http://localhost:9190/contracts/{id}
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "depositBtcAddress": "mu7gjSBLssPhKYuYU4qqBGFzjbh7ZTA6uY",
    "transfers" : [ {
      "id" : "uDui0F8PIjldKyGm0rdd",
      "status" : "new",
      "createdAt" : "2018-01-21T20:24:52.057Z",
      "btcTransactionId" : "78b6c99c40451d7e46f2eb41cdb831d087fecd759b01e00fd69e34959b5bee25",
      "btcAmount" : "0.00100000",
      "btcToArkRate" : "1985.31000000",
      "btcFlatFee" : "0.00000000",
      "btcPercentFee" : "1.00000000",
      "btcTotalFee" : "0.00001000",
      "arkSendAmount" : "1.96545690"
    } ]
  }
}
```