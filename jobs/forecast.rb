require 'net/https'
require 'json'

# Forecast API Key from https://developer.forecast.io
forecast_api_key = ENV["Dark_Sky_Key"]

# Latitude, Longitude for location
forecast_location_lat = "45.429522"
forecast_location_long = "-75.689613"

# Unit Format
# "us" - U.S. Imperial
# "si" - International System of Units
# "uk" - SI w. windSpeed in mph
forecast_units = "us"

# Language
# check https://developer.forecast.io/docs/v2 for supported languages
lang = "en"

SCHEDULER.every '180m', :first_in => 0 do |job|
   begin
    uri = URI.parse("https://api.forecast.io")
    uri.path = "/forecast/#{forecast_api_key}/#{forecast_location_lat},#{forecast_location_long}"
    params = {
      :units   => forecast_units,
      :lang    => lang,
      :exclude => "alerts,flags"
    }
    uri.query = URI.encode_www_form(params)
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    http.verify_mode = OpenSSL::SSL::VERIFY_PEER
    response = http.request(Net::HTTP::Get.new(uri.request_uri))
p    if not response.kind_of? Net::HTTPSuccess then raise "Forecast HTTP error" end
    forecast = JSON.parse(response.body)
    if forecast.has_key?("code") then raise "Forecast Error: #{forecast['code']}: #{forecast['error']}" end
    forecast_current_temp = forecast["currently"]["temperature"].round
    forecast_current_icon = forecast["currently"]["icon"]
    if forecast.has_key?("minutely") then forecast_summary_key = "minutely"
    elsif forecast.has_key?("houly") then forecast_summary_key = "hourly"
    elsif forecast.has_key?("daily") then forecast_summary_key = "daily"
    else                                  forecast_summary_key = "currently" end
    forecast_summary = forecast[forecast_summary_key]["summary"]
    send_event('forecast', { temperature: "#{forecast_current_temp}&deg;", hour: "#{forecast_summary}"})
  rescue => e
    puts "\e[33mFor the forecast widget to work, you need to set up your API key and coordinates in jobs/forecast.rb file.\e[0m"
    puts "\e[33m#{e.message}\e[0m"
  end
end
