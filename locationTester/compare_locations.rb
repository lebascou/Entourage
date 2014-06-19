require 'rubygems'
require 'rest_client'
require 'json'
require 'oauth'

def google_places location, terms
	url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

	result = RestClient.get(url, {:params => {
			"sensor" => "false",
			"key" => "AIzaSyCW5wdfkRWvvtSBzxAwQp198dbx0z44tK0",
			"location" => location,
			"rankby" => "distance",
			"types" => "school|university",
			"name" => terms
		}})

	json_result = JSON.parse(result);

	places = Array.new

	json_result['results'].each do |entry|
		obj = Hash.new
		obj['name'] = entry['name']
		obj['address'] = entry['vicinity']
		obj['geolocation'] = entry['geometry']['location']['lat'].to_s + ', ' + entry['geometry']['location']['lng'].to_s
		obj['types'] = entry['types']
		obj['rating'] = entry['rating']

		places.push(obj)
	end

	return places
end

def facebook_places location, terms
	locationTab = location.split(",")

	token = "CAACEdEose0cBAC8BfJiYgzB7WitwcpTUXtwjQMWSX71KPi9OgEAWPNcCZC86cjRdmOQ5UZC4pfrPPjwZCTQcZCCa1LWj7NgB3xN98cZBnZC8np4QktCvAyklaT5oVSEwAE7pUjRuD7bhkupzDZAjPL8lb4m9fjXlBsW2BRa3qDCS9GNZBLQWrk3dWLot0K4BjWDV9ukPGOZC9RgZDZD"
	name_condition = terms.empty? ? '' : ('name="' + terms + '" AND ');

	fql_request = 'SELECT name, type '
	fql_request += 'FROM page WHERE ' + name_condition + 'page_id IN '
    fql_request += '(SELECT page_id FROM place WHERE '
    fql_request += 'distance(latitude, longitude, "' + locationTab[0] + '", "' + locationTab[1] + '") < 50000 '
	fql_request += 'ORDER BY distance(latitude, longitude, "' + locationTab[0] + '", "' + locationTab[1] + '") LIMIT 100)'
	url = "https://graph.facebook.com/v2.0/fql"

	result = RestClient.get(url, {:params => {
			 "q" => fql_request,
			 "access_token" => token,
			 "format" => "json",
			 "suppress_http_code" => "1"
		}})

	json_result = JSON.parse(result);

	places = Array.new

	return places if json_result['data'].nil?
	json_result['data'].each do |entry|
		obj = Hash.new
		obj['name'] = entry['name']
		#obj['address'] = entry['vicinity']
		#obj['geolocation'] = entry['geometry']['location']['lat'].to_s + ', ' + entry['geometry']['location']['lng'].to_s
		obj['types'] = entry['type']
		#obj['rating'] = entry['rating']

		places.push(obj)
	end

	return places
end

def yelp_places location, terms
	api_host = 'api.yelp.com'
	consumer_key = '-6oT9qoNqtobvzqOF61CHQ'
	consumer_secret = 'cJiXi1RjMbmjj8C1GamNNV2MPY4'
	token = 'GXdM7JySwdpDQ2JyRZAOnDDJj3Obb4tR'
	token_secret = 'Z8kt1KKsFpgTICk-efjU2KefBC4'

	path = "/v2/search?"
	path += "sort=" + (terms.empty? ? "1" : "0")
	#path += "&category_filter=Education"
	path += "&term=" + terms
	path += "&ll=" + location

	consumer = OAuth::Consumer.new(consumer_key, consumer_secret, {:site => "http://#{api_host}"})
	access_token = OAuth::AccessToken.new(consumer, token, token_secret)

	result = access_token.get(URI::encode(path)).body
	json_result = JSON.parse(result)

	places = Array.new

	json_result['businesses'].each do |entry|
		obj = Hash.new
		obj['name'] = entry['name']
		obj['address'] = entry['location']['display_address'].join(", ")
		#obj['geolocation'] = entry['geometry']['location']['lat'].to_s + ', ' + entry['geometry']['location']['lng'].to_s
		obj['types'] = entry['categories']
		obj['rating'] = entry['rating']
		obj['distance'] = entry['distance']

		places.push(obj)
	end

	return places
end

def foursquare_places location, terms
	url = "https://api.foursquare.com/v2/venues/search"

	result = RestClient.get(url, {:params => {
			"client_id" => "4K05VEM4Q3TN4W5RPNBIA1NKFX1MAJJAR0D4ZLSU0BX1TT5O",
			"client_secret" => "LIARYU1LIGX5FCK32G1QQ4ZHZMONHTWCOEGL5UEFAGH2OBNH",
			"v" => "20140530",
			"ll" => location,
			"limit" => "25", 
			"categoryId" => "4bf58dd8d48988d1a0941735", #Établissement universitaire -> all
#			"categoryId" => "4d4b7105d754a06376d81259", #vie nocturne -> all
			"query" => terms
		}})

	json_result = JSON.parse(result)

	places = Array.new

	json_result['response']['venues'].each do |entry|
		obj = Hash.new
		obj['name'] = entry['name']
		obj['address'] = (entry['location']['address'] || '') + ', ' + (entry['location']['city'] || '')
		#obj['geolocation'] = entry['geometry']['location']['lat'].to_s + ', ' + entry['geometry']['location']['lng'].to_s
		categories = ''
		entry['categories'].each do |c|
			categories += c['name'] + ' '
		end

		obj['types'] = categories
		obj['rating'] = entry['rating']
		obj['distance'] = entry['location']['distance']

		places.push(obj)
	end

	return places
end

location = "45.7657267,4.8274612"
terms = ARGV[0] || ""
display_limit = 5

# Rate limit: ?
puts "\r\nFacebook Places API:"
puts facebook_places(location, terms)

# Rate limit: 100'000 calls per day
puts "\r\nGoogle Places API:"
puts google_places(location, terms)[0..display_limit]

# Rate limit: 10'000 calls per day
puts "\r\nYELP Places API:"
puts yelp_places(location, terms)[0..display_limit]

# Rate limit: 5'000 calls per hour -> 120'000 calls per day
puts "\r\nFourSquare Places API:"
puts foursquare_places(location, terms)[0..display_limit]
