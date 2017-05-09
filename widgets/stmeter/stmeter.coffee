class Dashing.Stmeter extends Dashing.Widget
  constructor: ->
    super
    @queryState()
    @observe 'power', (power) ->
      $(@node).find(".stmeter").val(power).trigger('change')
  
  @accessor 'power', Dashing.AnimatedValue
 
  @accessor 'totalizedEnergy', ->
    e = @get('energy')
    "Total Usage: #{e} kWh"
  
  queryState: ->
    $.get '/smartthings/dispatch',
      widgetId: @get('id'),
      deviceType: 'power',
      deviceId: @get('device')
      (data) =>
        json = JSON.parse data
        @set 'power', json.power
        @set 'energy', json.energy

  ready: ->
    stmeter = $(@node).find(".stmeter")
    stmeter.attr("data-bgcolor", stmeter.css("background-color"))
    stmeter.attr("data-fgcolor", stmeter.css("color"))
    stmeter.knob()
  
  onData: (data) ->