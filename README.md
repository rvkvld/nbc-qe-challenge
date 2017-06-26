# Assumptions:

* Testing only `/planetary/sounds` (beta) API endpoint
* No requirements found to actually cover the endpoint with automated tests, so showing only the approach to testing
* No actual api_key will be generated. DEMO_KEY will be used for demo automated tests
* Covering only functional and basic security testing. Not considering any other types (i.e. performance, load, stress etc.) 

# Endpoint Documentation

(copied over from the [NASA Website](https://api.nasa.gov/api.html#sounds))

#### HTTP Request: 
`GET https://api.nasa.gov/planetary/sounds`

#### QUERY PARAMETERS

| Parameter | Type | Default | Description |
| --------- | ---- | ------- | ----------- |
| q | string | None | Search text to filter results | 
| limit | int | 10 |number of tracks to return |
| api_key | string | DEMO_KEY | api.nasa.gov key for expanded usage |

# Found Issues:

* **q** parameter doesn't work. 
	* To reproduce, navigate to [/planetary/sounds?api_key=DEMO_KEY&q=asdfljahsflajshdflaskjdfhsf](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&q=asdfljahsflajshdflaskjdfhsf)
	* Expected results: no records returned because "asdfljahsflajshdflaskjdfhsf" is not found in any of them
	* Actual results: 10 default records are returned
	* Notes: same issue is observed even if the filter is valid (i.e. [Interstellar](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&q=Interstellar))
* Server returns 500 internal server error for invalid **limit** values
	* Data Driven Testing

| Parameter Value | Expected Results | Actual Results |
| --------------- | ---------------- | -------------- |
| [0](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=0) | 0 records returned | 0 records returned (NOT A BUG) |
| [1](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=1) | 1 record returned | 1 record returned (NOT A BUG) |
| [no parameter value](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY) |  (default value) 10 records returned | 10 records returned (NOT A BUG) |
| [-1](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=-1) | "Invalid Limit Value" error message returned | (default value) 10 records returned ![](https://placehold.it/15/ff0000/000000?text=+) **BUG** |
| [-2](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=-2) | "Invalid Limit Value" error message returned | 500 internal server error and stack trace returned ![](https://placehold.it/15/ff0000/000000?text=+) **BUG** |
| [2147483647](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=2147483647) | all records returned | all records (64 of them) returned (NOT A BUG) |
| [2147483648](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=2147483648) | "Invalid Limit Value" error message returned | 500 internal server error and stack trace returned ![](https://placehold.it/15/ff0000/000000?text=+) **BUG** |
| [stringValue](https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY&limit=stringValue) | "Invalid Limit Value" error message returned | 500 internal server error and stack trace returned ![](https://placehold.it/15/ff0000/000000?text=+) **BUG** |


* Documentation bug: Default value (DEMO_KEY) not applied. According to requirements (most likely it's a bug in requirements), "DEMO_KEY" should be a default value. However, if we run [planetary/sounds](https://api.nasa.gov/planetary/sounds), we see API_KEY_MISSING error message

# Approach to *functional* Testing:

* Since there are not so many parameters, we can test each of them individually
* `api_key`:
	* Verify that `DEMO_KEY` key works and displays demo data
	* Verify that an actual key for a registered user works and displays data
	* Try to omit the `api_key` paramter. It should report an error (if DEMO_KEY is not supposed to be default value, though requirments say it should)
	* Try to provide invalid api_key, verify that it reports an error
	* Apply some basic SQL Injection techniques (try to send `" or ""="` and `" or 1=1`)
* `q`:
	* Verify that a query without this parameter displays all records since no filters applied
	* Verify that queries with valid values (i.e. "Interstallar") return only records that have these filter values
	* Verify that queries with values that are not present in any records return 0 records back
	* Run a few boundary and specifal-scenarios tests (special symbols, min-max length, languages etc.)
* `limit`:
	* Verify that default value gets applied and 10 records are returned if no parameter value specified
	* Verify that `limit` works with valid values (i.e. 5 and 24) 
	* Do some boundary testing:
		* Verify that `limit = 0` returns 0 records
		* Verify that `limit = MAX_INT` returns all present records
		* Verify that `limit = -1` returns a human-readable error
		* Verify that `limit = MAX_INT + 1` returns a human readable error
	* Run a few negative tests:
		* Verify that `limit = invalidStringValue` returns a human readable error
		* Verify that `limit = 1.5` returns a human readable error
* I'd also verify that for valid querries correctly-formatted responses are returned (with all necessary keys-value pairs)

# Examples of test cases

| ID | Title | Prerequisites | Steps | Expected Results | Actual Results | PASS/FAIL | Suite |
| -- | ----- | ----- | ---------------- | -------------- | --------- | ----- |
| 1  | Default value is applied when request is sent without `limit` parameter | At least 10 records should be present in the database | 1. Send request with no `limit` parameter: https://api.nasa.gov/planetary/sounds?api_key=DEMO_KEY | In JSON response `count` = 10 and `results` array has 10 dictionaries with records | | | Smoke |

