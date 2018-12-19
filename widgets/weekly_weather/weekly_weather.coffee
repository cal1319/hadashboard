class Dashing.WeeklyWeather extends Dashing.ClickableWidget

  constructor: ->
    super
  onClick: (node, event) ->
  	Dashing.cycleDashboardsNow(boardnumber: ('radar'), stagger: ("false"), fastTransition:('true'), transitiontype: @get('transitiontype'))
  
  
###
  constructor: ->
    super

    @skycons = new Skycons
      'color': 'white'
    @skycons.play()

  ready: ->
  
  onData: (data) ->
    #@setSkycon(data)
  

    
    
  setSkycon: (data) ->
  
    $('.weather-icon').each ->
        t = $(@)
        console.log t
  
    for key of data.forecasts
      if data.forecasts.hasOwnProperty(key)
        
        code = data.forecasts[key].code
        element = data.forecasts[key].element
        
        icon = @toSkycon(code)
        #console.log icon
    
        @skycons.set element, eval(icon)
        #console.info key + ': ' + data.forecasts[key].code

        

  
    console.log "weather icon is " + icon
    

    
    
  toSkycon: (data) ->
    'Skycons.' + data.replace(/-/g, "_").toUpperCase()


###



 
