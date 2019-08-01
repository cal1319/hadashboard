/**
 *  Dashing Access
 *
 *  Copyright 2017 florianz/bmmiller
 *
 *  Author: florianz/bmmiller
 *  Contributor: bmmiller, Dianoga, mattjfrank, ronnycarr
 *
 */


//
// Definition
//
definition(
    name: "Dashing Access",
    namespace: "bmmiller",
    author: "florianz",
    description: "API access for Dashing dashboards.",
    category: "Convenience",
    iconUrl: "http://res.cloudinary.com/dmerj1afs/image/upload/v1490651301/hadashboard-icon_uw77s9.png",
    iconX2Url: "http://res.cloudinary.com/dmerj1afs/image/upload/v1490651301/hadashboard-icon_uw77s9.png",
    oauth: true) {
}


//
// Preferences
//
preferences {
    section("Allow access to the following things...") {
        input "contacts", "capability.contactSensor", title: "Which contact sensors?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "meters", "capability.powerMeter", title: "Which meters?", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Which motion sensors?", multiple: true, required: false
        input "presences", "capability.presenceSensor", title: "Which presence sensors?", multiple: true, required: false
        input "dimmers", "capability.switchLevel", title: "Which dimmers?", multiple: true, required: false
        input "switches", "capability.switch", title: "Which switches?", multiple: true, required: false
        input "temperatures", "capability.temperatureMeasurement", title: "Which temperature sensors?", multiple: true, required: false
        input "humidities", "capability.relativeHumidityMeasurement", title: "Which humidity sensors?", multiple: true, required: false
        input "batteries", "capability.battery", title: "Which battery sensors?", multiple: true, required: false
        input "garagedoors", "capability.doorControl", title: "Which garage doors?", multiple: true, required: false
		input "thermostats", "capability.thermostat", title: "Which thermostats?", multiple: true, required: false
    }
}


//
// Mappings
//
mappings {
    path("/config") {
        action: [
            GET: "getConfig",
            POST: "postConfig"
        ]
    }
    path("/contact") {
        action: [
            GET: "getContact"
        ]
    }
    path("/lock") {
        action: [
            GET: "getLock",
            POST: "postLock"
        ]
    }
    path("/mode") {
        action: [
            GET: "getMode",
            POST: "postMode"
        ]
    }
    path("/motion") {
        action: [
            GET: "getMotion"
        ]
    }
    path("/phrase") {
        action: [
            POST: "postPhrase"
        ]
    }
    path("/power") {
        action: [
            GET: "getPower"
        ]
    }
    path("/presence") {
        action: [
            GET: "getPresence"
        ]
    }
    path("/dimmer") {
        action: [
            GET: "getDimmer",
            POST: "postDimmer"
        ]
    }
    path("/dimmer/level") {
        action: [
            POST: "dimmerLevel"
        ]
    }
    path("/switch") {
        action: [
            GET: "getSwitch",
            POST: "postSwitch"
        ]
    }
    path("/temperature") {
        action: [
            GET: "getTemperature"
        ]
    }
    path("/humidity") {
        action: [
            GET: "getHumidity"
        ]
    }
    path("/weather") {
        action: [
            GET: "getWeather"
        ]
    }
    path("/battery") {
        action: [
            GET: "getBattery"
        ]
    }
    path("/garage") {
    	action: [
        	GET: "getGarage",
            POST: "postGarage"
        ]
    }
	path("/thermostat") {
    	action: [
        	GET: "getThermostat",
            POST: "postThermostat"
        ]
    }
}


//
// Installation
//
def installed() {
    initialize()
}

def updated() {
	log.trace "Dashing Access: Updated, Unsubscribing..."
    unsubscribe()
    initialize()
}

def initialize() {
	log.trace "Dashing Access: Initializing..."
    state.dashingURI = ""
    state.dashingAuthToken = ""
    state.widgets = [
        "contact": [:],
        "lock": [:],
        "mode": [:],
        "motion": [:],
        "power": [:],
        "presence": [:],
        "dimmer": [:],
        "switch": [:],
        "temperature": [:],
        "humidity": [:],
        "battery": [:],
        "garagedoor": [:],
		"thermostat": [:]
        ]

    subscribe(contacts, "contact", contactHandler)
    subscribe(location, "mode", locationHandler)
    subscribe(locks, "lock", lockHandler)
    subscribe(motions, "motion", motionHandler)
    subscribe(meters, "power", meterPowerHandler)
	subscribe(meters, "energy", meterEnergyHandler)
    subscribe(presences, "presence", presenceHandler)
    subscribe(dimmers, "switch", dimmerSwitch)
    subscribe(dimmers, "level", dimmerHandler)
    subscribe(switches, "switch", switchHandler)
    subscribe(temperatures, "temperature", temperatureHandler)
    subscribe(humidities, "humidity", humidityHandler)
    subscribe(batteries, "battery", batteryHandler)
    subscribe(garagedoors, "door", garageDoorHandler)
	subscribe(thermostats, "temperature", thermostatTempHandler)
    subscribe(thermostats, "heatingSetpoint", thermostatHeatSPHandler)
    subscribe(thermostats, "coolingSetpoint", thermostatCoolSPHandler)
    subscribe(thermostats, "operatingState", thermostatOpStateHandler)
    subscribe(thermostats, "thermostatMode", thermostatModeHandler)
    subscribe(thermostats, "thermostatFanMode", thermostatFanModeHandler)
    log.trace "Dashing Access: Initialzed and Subscriptions made"
}


//
// Config
//
def getConfig() {
    ["dashingURI": state.dashingURI, "dashingAuthToken": state.dashingAuthToken]
}

def postConfig() {
    state.dashingURI = request.JSON?.dashingURI
    state.dashingAuthToken = request.JSON?.dashingAuthToken
    respondWithSuccess()
}

//
// Contacts
//
def getContact() {
    def deviceId = request.JSON?.deviceId
    log.debug "getContact ${deviceId}"

    if (deviceId) {
        registerWidget("contact", deviceId, request.JSON?.widgetId)

        def whichContact = contacts.find { it.displayName == deviceId }
        if (!whichContact) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "state": whichContact.currentContact]
        }
    }

    def result = [:]
    contacts.each {
        result[it.displayName] = [
            "state": it.currentContact,
            "widgetId": state.widgets.contact[it.displayName]]}

    return result
}

def contactHandler(evt) {
    def widgetId = state.widgets.contact[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Locks
//
def getLock() {
    def deviceId = request.JSON?.deviceId
    log.debug "getLock ${deviceId}"

    if (deviceId) {
        registerWidget("lock", deviceId, request.JSON?.widgetId)

        def whichLock = locks.find { it.displayName == deviceId }
        if (!whichLock) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "state": whichLock.currentLock]
        }
    }

    def result = [:]
    locks.each {
        result[it.displayName] = [
            "state": it.currentLock,
            "widgetId": state.widgets.lock[it.displayName]]}

    return result
}

def postLock() {
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "postLock ${deviceId}, ${command}"

    if (command && deviceId) {
        def whichLock = locks.find { it.displayName == deviceId }
        if (!whichLock) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichLock."$command"()
        }
    }
    return respondWithSuccess()
}

def lockHandler(evt) {
    def widgetId = state.widgets.lock[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Meters
//
def getPower() {
    def deviceId = request.JSON?.deviceId
    log.debug "getPower ${deviceId}"

    if (deviceId) {
        registerWidget("power", deviceId, request.JSON?.widgetId)

        def whichMeter = meters.find { it.displayName == deviceId }
        if (!whichMeter) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "power": whichMeter.currentValue("power"),
				"energy": whichMeter.currentValue("energy")]
        }
    }

    def result = [:]
    meters.each {
        result[it.displayName] = [
            "power": it.currentValue("power"),	
			"energy": it.currentValue("energy"),
            "widgetId": state.widgets.power[it.displayName]]}

    return result
}

def meterPowerHandler(evt) {
    def widgetId = state.widgets.power[evt.displayName]
    notifyWidget(widgetId, ["power": (Math.round(Double.parseDouble(evt.value)))])
}

def meterEnergyHandler(evt) {
    def widgetId = state.widgets.power[evt.displayName]
    notifyWidget(widgetId, ["energy": evt.value])
}

//
// Modes
//
def getMode() {
    def widgetId = request.JSON?.widgetId
    
    if (widgetId) {
        if (!state['widgets']['mode'].containsKey(widgetId)) {
            state['widgets']['mode'].put(widgetId, widgetId)   
            log.debug "registerWidget for mode: ${widgetId}"
        }
    }

    log.debug "getMode: ${location.mode}"
    return ["mode": location.mode]
}

def postMode() {
    def mode = request.JSON?.mode
    log.debug "postMode ${mode}"

    if (mode) {
        setLocationMode(mode)        	
    }

    if (location.mode != mode) {
        return respondWithStatus(404, "Mode not found.")
    }
    return respondWithSuccess()
}

def locationHandler(evt) {
    for (i in state['widgets']['mode']) {
        notifyWidget(i.value, ["mode": evt.value])
    }
}

//
// Motions
//
def getMotion() {
    def deviceId = request.JSON?.deviceId
    log.debug "getMotion ${deviceId}"

    if (deviceId) {
        registerWidget("motion", deviceId, request.JSON?.widgetId)

        def whichMotion = motions.find { it.displayName == deviceId }
        if (!whichMotion) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "state": whichMotion.currentMotion]
        }
    }

    def result = [:]
    motionss.each {
        result[it.displayName] = [
            "state": it.currentMotion,
            "widgetId": state.widgets.motion[it.displayName]]}

    return result
}

def motionHandler(evt) {
    def widgetId = state.widgets.motion[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Phrases
//
def postPhrase() {
    def phrase = request.JSON?.phrase
    log.debug "postPhrase ${phrase}"

    if (!phrase) {
        respondWithStatus(404, "Phrase not specified.")
    }

    location.helloHome.execute(phrase)

    return respondWithSuccess()

}

//
// Presences
//
def getPresence() {
    def deviceId = request.JSON?.deviceId
    log.debug "getPresence ${deviceId}"

    if (deviceId) {
        registerWidget("presence", deviceId, request.JSON?.widgetId)

        def whichPresence = presences.find { it.displayName == deviceId }
        if (!whichPresence) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "state": whichPresence.currentPresence]
        }
    }

    def result = [:]
    presences.each {
        result[it.displayName] = [
            "state": it.currentPresence,
            "widgetId": state.widgets.presence[it.displayName]]}

    return result
}

def presenceHandler(evt) {
    def widgetId = state.widgets.presence[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Dimmers
//
def getDimmer() {
    def deviceId = request.JSON?.deviceId
    log.debug "getDimmer ${deviceId}"

    if (deviceId) {
        registerWidget("dimmer", deviceId, request.JSON?.widgetId)

        def whichDimmer = dimmers.find { it.displayName == deviceId }
        if (!whichDimmer) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "level": whichDimmer.currentValue("level"),
                "state": whichDimmer.currentValue("switch")
            ]
        }
    }

    def result = [:]
    dimmers.each {
        result[it.displayName] = [
            "state": it.currentValue("switch"),
            "level": it.currentValue("level"),
            "widgetId": state.widgets.dimmer[it.displayName]]}

    return result
}

def postDimmer() {
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "postDimmer ${deviceId}, ${command}"

    if (command && deviceId) {
        def whichDimmer = dimmers.find { it.displayName == deviceId }
        if (!whichDimmer) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichDimmer."$command"()
        }
    }
    return respondWithSuccess()
}

def dimmerLevel() {
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "dimmerLevel ${deviceId}, ${command}"
    command = command.toInteger()
    if (command && deviceId) {
        def whichDimmer = dimmers.find { it.displayName == deviceId }
        if (!whichDimmer) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichDimmer.setLevel(command)
        }
    }
    return respondWithSuccess()
}

def dimmerHandler(evt) {
    def widgetId = state.widgets.dimmer[evt.displayName]
    pause(1000)
    notifyWidget(widgetId, ["level": evt.value])
}

def dimmerSwitch(evt) {
    def whichDimmer = dimmers.find { it.displayName == evt.displayName }
    def widgetId = state.widgets.dimmer[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Switches
//
def getSwitch() {
    def deviceId = request.JSON?.deviceId
    log.debug "getSwitch ${deviceId}"

    if (deviceId) {
        registerWidget("switch", deviceId, request.JSON?.widgetId)

        def whichSwitch = switches.find { it.displayName == deviceId }
        if (!whichSwitch) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "switch": whichSwitch.currentSwitch]
        }
    }

    def result = [:]
    switches.each {
        result[it.displayName] = [
            "state": it.currentSwitch,
            "widgetId": state.widgets.switch[it.displayName]]}

    return result
}

def postSwitch() {
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "postSwitch ${deviceId}, ${command}"

    if (command && deviceId) {
        def whichSwitch = switches.find { it.displayName == deviceId }
        if (!whichSwitch) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichSwitch."$command"()
        }
    }
    return respondWithSuccess()
}

def switchHandler(evt) {
    def widgetId = state.widgets.switch[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Temperatures
//
def getTemperature() {
    def deviceId = request.JSON?.deviceId
    log.debug "getTemperature ${deviceId}"

    if (deviceId) {
        registerWidget("temperature", deviceId, request.JSON?.widgetId)

        def whichTemperature = temperatures.find { it.displayName == deviceId }
        if (!whichTemperature) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "value": whichTemperature.currentTemperature]
        }
    }

    def result = [:]
    temperatures.each {
        result[it.displayName] = [
            "value": it.currentTemperature,
            "widgetId": state.widgets.temperature[it.displayName]]}

    return result
}

def temperatureHandler(evt) {
    def widgetId = state.widgets.temperature[evt.displayName]
    notifyWidget(widgetId, ["value": evt.value])
}

//
// Humidities
//
def getHumidity() {
    def deviceId = request.JSON?.deviceId
    log.debug "getHumidity ${deviceId}"

    if (deviceId) {
        registerWidget("humidity", deviceId, request.JSON?.widgetId)

        def whichHumidity = humidities.find { it.displayName == deviceId }
        if (!whichHumidity) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "value": whichHumidity.currentHumidity]
        }
    }

    def result = [:]
    humidities.each {
        result[it.displayName] = [
            "value": it.currentHumidity,
            "widgetId": state.widgets.humidity[it.displayName]]}

    return result
}

def humidityHandler(evt) {
    def widgetId = state.widgets.humidity[evt.displayName]
    notifyWidget(widgetId, ["value": evt.value])
}

//
// Weather
//
def getWeather() {
    def feature = request.JSON?.feature
    if (!feature) {
        feature = "conditions"
    }
    return getWeatherFeature(feature)
}

//
// Batteries
//
def getBattery() {
    def deviceId = request.JSON?.deviceId
    log.trace "getBattery ${deviceId}"

    if (deviceId) {
        registerWidget("battery", deviceId, request.JSON?.widgetId)

        def whichBattery = batteries.find { it.displayName == deviceId }
        if (!whichBattery) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "value": whichBattery.latestValue("battery")]
        }
    }

    def result = [:]
    batteries.each {
        result[it.displayName] = [
            "value": it.latestValue("battery"),
            "widgetId": state.widgets.battery[it.displayName]]}

    return result
}

def batteryHandler(evt) {
    def widgetId = state.widgets.battery[evt.displayName]
    notifyWidget(widgetId, ["value": evt.value])
}

//
// Garage Door
//
def getGarage() {
    def deviceId = request.JSON?.deviceId
    log.debug "getGarage ${deviceId}"

    if (deviceId) {
        registerWidget("garagedoor", deviceId, request.JSON?.widgetId)

        def whichGarageDoor = garagedoors.find { it.displayName == deviceId }
        if (!whichGarageDoor) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
        	log.debug "${whichGarageDoor}"
            return [
                "deviceId": deviceId,
                "state": whichGarageDoor.currentDoor]
        }
    }

    def result = [:]
    garagedoors.each {
        result[it.displayName] = [
            "state": it.currentDoor,
            "widgetId": state.widgets.garagedoor[it.displayName]]}

    return result
}

def postGarage() {
	log.debug "postGarage ${request.JSON}"
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "postGarage ${deviceId}, ${command}"

    if (command && deviceId) {
        def whichGarage = garagedoors.find { it.displayName == deviceId }
        if (!whichGarage) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichGarage."$command"()
        }
    }
    return respondWithSuccess()		
}

def garageDoorHandler(evt) {
    def widgetId = state.widgets.garagedoor[evt.displayName]
    notifyWidget(widgetId, ["state": evt.value])
}

//
// Thermostat
//
def getThermostat() {
    def deviceId = request.JSON?.deviceId
    log.debug "getThermostat ${deviceId}"

    if (deviceId) {
        registerWidget("thermostat", deviceId, request.JSON?.widgetId)

        def whichThermostat = thermostats.find { it.displayName == deviceId }
        if (!whichThermostat) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            return [
                "deviceId": deviceId,
                "temperature": whichThermostat.currentTemperature,
                "heatingSetpoint": whichThermostat.currentHeatingSetpoint,
                "coolingSetpoint": whichThermostat.currentCoolingSetpoint,
                "stateOperating": whichThermostat.currentThermostatOperatingState,
                "mode": whichThermostat.currentThermostatMode,
                "fan": whichThermostat.currentThermostatFanMode]
        }
    }

    def result = [:]
    thermostats.each {
		log.debug "getThemostat:"
        result[it.displayName] = [
            "temperature": it.currentTemperature,
			"heatingSetpoint": it.currentHeatingSetpoint,
			"coolingSetpoint": it.currentCoolingSetpoint,
            "stateOperating": it.currentThermostatOperatingState,  
            "mode": it.currentThermostatMode,
            "modeFan": it.currentThermostatFanMode,
            "widgetId": state.widgets.thermostat[it.displayName]]}

    return result
}

def postThermostat() {
    def command = request.JSON?.command
    def deviceId = request.JSON?.deviceId
    log.debug "postThermostat: ${request.JSON}"

    if (command && deviceId) {
        def whichThermostat = thermostats.find { it.displayName == deviceId }
        if (!whichThermostat) {
            return respondWithStatus(404, "Device '${deviceId}' not found.")
        } else {
            whichThermostat."$command"()
        }
    }
    return respondWithSuccess()
}

def thermostatTempHandler(evt) { 
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["temperature": evt.value])
}

def thermostatHeatSPHandler(evt) {
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["heatingSetpoint": evt.value])
}

def thermostatCoolSPHandler(evt) {
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["coolingSetpoint": evt.value])
}

def thermostatOpStateHandler(evt) {
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["operatingState": evt.value])
}

def thermostatModeHandler(evt) {
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["mode": evt.value])
}

def thermostatFanModeHandler(evt) {
    def widgetId = state.widgets.thermostat[evt.displayName]
    notifyWidget(widgetId, ["modeFan": evt.value])
}

//
// Widget Helpers
//
private registerWidget(deviceType, deviceId, widgetId) {
    if (deviceType && deviceId && widgetId) {
        state['widgets'][deviceType][deviceId] = widgetId
        log.debug "registerWidget ${deviceType}:${deviceId}@${widgetId}"
    }
}

private notifyWidget(widgetId, data) {	
    if (widgetId && state.dashingAuthToken) {
        def uri = getWidgetURI(widgetId)
        data["auth_token"] = state.dashingAuthToken
        log.debug "notifyWidget ${uri} ${data}"
        httpPostJson(uri, data)
    }
}

private getWidgetURI(widgetId) {
    state.dashingURI + "/widgets/${widgetId}"
}


//
// Response Helpers
//
private respondWithStatus(status, details = null) {
    def response = ["error": status as Integer]
    if (details) {
        response["details"] = details as String
    }
    return response
}

private respondWithSuccess() {
    return respondWithStatus(0)
}