class Dashing.Stthermostat extends Dashing.Widget
  constructor: ->
    super
    @queryState()

  @accessor 'temperature',
    get: -> @_temperature ? "00"
    set: (key, value) -> @_temperature = value

  @accessor 'mode',
    get: -> @_mode ? "auto"
    set: (key, value) -> @_mode = value

  @accessor 'stateOperating',
    get: -> @_stateOperating ? "idle"
    set: (key, value) -> @_stateOperating = value

  @accessor 'modeFan',
    get: -> @_modeFan ? "auto"
    set: (key, value) -> @_modeFan = value

  @accessor 'coolingSetpoint',
    get: -> @_coolingSetpoint ? "00"
    set: (key, value) -> @_coolingSetpoint = value

  @accessor 'heatingSetpoint',
    get: -> @_heatingSetpoint ? "00"
    set: (key, value) -> @_heatingSetpoint = value

  @accessor 'icon',
    get: ->
      if @get('mode') == 'auto'
        '/auto.png'
      else if @get('mode') == 'cool'
        '/cool.png'
      else if @get('mode') == 'heat'
        '/heat.png'
      else if @get('mode') == 'emergency heat'
        '/emer.png'
      else
        '/off.png'
    set: Batman.Property.defaultAccessor.set

  @accessor 'iconFan',
    get: ->
      if @get('modeFan') == 'fanAuto' || @get('modeFan') == 'auto'
        '/auto.png'
      else if @get('modeFan') == 'fanOn' || @get('modeFan') == 'on'
        '/fanon.png'
      else
        '/circ.png'
    set: Batman.Property.defaultAccessor.set

  @accessor 'modeInverse', ->
    if @get('mode') == 'auto'
      'cool'
    else if @get('mode') == 'cool'
      'heat'
    else if @get('mode') == 'heat'
      "emergency heat"
    else if @get('mode') == 'emergency heat'
      'off'
    else
      'auto'

  @accessor 'modeInverseFan', ->
    if @get('modeFan') == 'fanAuto' || @get('modeFan') == 'auto'
      'fanOn'
    else if @get('modeFan') == 'fanOn' || @get('modeFan') == 'on'
      'fanCirculate'
    else
      'fanAuto'

  toggleMode: ->
    newMode = @get 'modeInverse'
    @set 'mode', newMode
    return newMode

  toggleModeFan: ->
    newMode = @get 'modeInverseFan'
    @set 'modeFan', newMode
    return newMode

  plusTempCool: ->
    @set 'coolingSetpoint', parseInt(@get('coolingSetpoint'),10)+1
    return parseInt(@get('coolingSetpoint'),10)+1

  minusTempCool: ->
    @set 'coolingSetpoint', parseInt(@get('coolingSetpoint'),10)-1
    return parseInt(@get('coolingSetpoint'),10)-1

  plusTempHeat: ->
    @set 'heatingSetpoint', parseInt(@get('heatingSetpoint'),10)+1
    return parseInt(@get('heatingSetpoint'),10)+1

  minusTempHeat: ->
    @set 'heatingSetpoint', parseInt(@get('heatingSetpoint'),10)-1
    return parseInt(@get('heatingSetpoint'),10)-1

  queryState: ->
    $.get '/smartthings/dispatch',
      widgetId: @get('id'),
      deviceType: 'thermostat',
      deviceId: @get('device')
      (data) =>
        json = JSON.parse data
        @set 'temperature', json.temperature
        @set 'coolingSetpoint', json.coolingSetpoint
        @set 'heatingSetpoint', json.heatingSetpoint
        @set 'mode', json.mode
        @set 'modeFan', json.modeFan
        @set 'stateOperating', json.stateOperating


  postMode: ->
    newMode = @toggleMode()
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: newMode,
      (data) =>
        json = JSON.parse data
        if json.error != 0
          @toggleMode()

  postModeFan: ->
    newMode = @toggleModeFan()
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: newMode,
      (data) =>
        json = JSON.parse data
        if json.error != 0
          @toggleModeFan()

  coolUp: ->
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: "raiseCoolSetpoint",
      (data) =>
        json = JSON.parse data
        @plusTempCool()


  coolDown: ->
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: "lowerCoolSetpoint",
      (data) =>
        json = JSON.parse data
        @minusTempCool()

  heatUp: ->
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: 'raiseHeatingSetpoint',
      (data) =>
        json = JSON.parse data
        @plusTempHeat()


  heatDown: ->
    $.post '/smartthings/dispatch',
      deviceType: 'thermostat',
      deviceId: @get('device'),
      command: 'lowerHeatingSetpoint',
      (data) =>
        json = JSON.parse data
        @minusTempHeat()

  toggleSettings: ->
    $('.widget-stthermostat .info').hide()
    $('.widget-stthermostat .controls').fadeIn()
    setTimeout ()->
      $('.widget-stthermostat .controls').fadeOut(300)
      setTimeout ()->
        $('.widget-stthermostat .info').fadeIn(1000)
      ,300
    ,15000

  ready: ->

  onData: (data) ->


  onClick: (node, event) ->
    dataSpot = event.toElement.className

    if dataSpot == "fa fa-minus-square cool"
      @coolDown()
    else if dataSpot == "fa fa-plus-square cool"
      @coolUp()
    else if dataSpot == "fa fa-minus-square heat"
      @heatDown()
    else if dataSpot == "fa fa-plus-square heat"
      @heatUp()
    else if dataSpot == "mode" || dataSpot == "mode-icon"
     @postMode()
    else if dataSpot == "mode fan" || dataSpot == "fan"
     @postModeFan()
    else if dataSpot == "toggle-area"
     @toggleSettings()