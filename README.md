# IP Address Management REST API
 
## Requirements

Create a simple IP Address Management REST API using Spring Framework on top of any data store. It will include the ability to add IP Addresses by CIDR block and then either acquire or release IP addresses individually. Each IP address will have a status associated with it that is either “available” or “acquired”. 
 
The REST API must support four endpoint:
  * **Create IP addresses** - take in a CIDR block (e.g. 10.0.0.1/24) and add all IP addresses within that block to the data store with status “available”
  * **List IP addresses** - return all IP addresses in the system with their current status
  * **Acquire an IP** - set the status of a certain IP to “acquired”
  * **Release an IP** - set the status of a certain IP to “available”


## Approach

* [IPAddress by seancfoley](https://github.com/seancfoley/IPAddress) for parsing and validation
 of CIDR ranges.
* [H2 in-memory DB](https://h2database.com/html/main.html) for quick prototyping.

## Run

Run with `./mvnw clean spring-boot:run`

Test with `./mvnw test`

## API Endpoints

|Method|Route                    |Description|
|:----:|-------------------------|-----------|
|`GET` |`/addresses`             |Returns a list of all addresses.|
|`POST`|`/addresses`             |Creates new addresses from a range. Expects a JSON payload like: `{ "range": "1.1.1.0/24" }`. If called multiple times with overlapping ranges it will only create new addresses and not modify ones that already exist. This is so that acquired addresses don't get reset to available. Returns a list of all addresses in that range and their current status.|
|`GET` |`/addresses/{ip}`        |Returns a single address and it's status.|
|`POST`|`/addresses/{ip}/acquire`|Changes an address's status to "acquired". Will return a `509 Conflict` if the address's status is not "available" to prevent data races.|
|`PUT` |`/addresses/{ip}/release`|Changes an address's status to "available" no matter the previous status. This endpoint uses `PUT` since it is idempotent.|

## Future Considerations

* The approach should be adapted if IPv6 or large IPv4 ranges are considered typical usage
 (although it theoretically works right now, it is inefficient when handling large ranges).
* IPs ending in 0 (subnet identifier) and 255 (local broadcast) are typically reserved. Should the
 system allow acquiring those or even list them as available?
* The DB Key (which is currently the IP) should likely be numerical and not a string so that `1.1.1.2` will come sorted before `1.1.1.12`.
What exactly the key should be requires more specifics (such as the specific DB) to answer
 properly. For example, [PostgreSQL has the cidr and inet types to represent networks](https://www.postgresql.org/docs/9.1/datatype-net-types.html).
* This API could easily be turned into a hypertext-driven REST API using [Spring HATEOAS](https://spring.io/projects/spring-hateoas). It currently functions more as a RPC API for simplicity.