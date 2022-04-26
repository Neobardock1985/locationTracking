import React from 'react';
import { View, NativeModules, AppRegistry, DeviceEventEmitter } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
//import MapboxGL from '@react-native-mapbox-gl/maps'
import { Helper } from '../managers/Helper';

import { styles } from '../styles/styles';

var globalLocationPaths = [];

const connectionStatusModule = NativeModules.ConnectionStatusModule;

AppRegistry.registerHeadlessTask('MapHeadlessTask', () => MapHeadlessTask);

const MapHeadlessTask = async (data) => {
    console.log("MapHeadlessTask")
    await instance.saveLocation(data.lat, data.lng);
    Helper.positionDetected = true;
    connectionStatusModule.destroyCurrentPosService(() => { });
}


class MapaScreen extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            showMapboxMap: false,
            defaultCenterCoords: [0, 0],
            gpsText: 'Detectando ubicación',
        };

        instance = this;
    }


    async processTrackingInfo() {
        Helper.currentTaskId = "currentTaskId"
        Helper.positionDetected = false;

        this.listener = DeviceEventEmitter.addListener('onLocationChange', this.onLocationChange, false);
        let paths = await this.getLocationPaths();
        console.log(paths, "paths");

        if (paths.length === 0) {
            connectionStatusModule.initGetCurrentPos('MapHeadlessTask', () => { console.warn('Servicio iniciado') });
        } else {
            Helper.positionDetected = true;
        }

        if (Helper.currentTaskId != "") {
            await this.startServiceLocation();
        }
    }


    async UNSAFE_componentWillMount() {
        this.processTrackingInfo();

        //screen gain focus
        this.gotFocus = this.props.navigation.addListener('focus', async () => {
            console.log("isFocused MapPage");
            this.processTrackingInfo();
        });

        //screen lose focus
        this.loseFocus = this.props.navigation.addListener('blur', () => {
            console.log("loseFocus MapPage");
            Helper.currentTaskId = "";
            Helper.positionDetected = false;
            this.stopServiceLocation();
        });

    }


    UNSAFE_componentWillUnmount() {
        this.gotFocus();
        this.loseFocus();
        if (this.listener) { this.listener.remove(); }
    }


    startServiceLocation = async () => {
        console.log("startServiceLocation");
        connectionStatusModule.ServiceBind();
    }

    stopServiceLocation = async () => {
        console.log("stopServiceLocation");
        connectionStatusModule.ServiceUnbind();
    }


    // ===================================================================
    // Helper functions
    // ===================================================================

    buildLineFeature(_storagePaths) {
        // Build the route to draw it on the map
        let _paths = [];
        if (_storagePaths) {
            _storagePaths.forEach(path => {
                _paths.push(path.coords);
            });
        }
        return _paths;
    }


    featureMaker(name, type, coord) {
        var feature = {
            "type": "Feature",
            'properties': {
                'name': name
            },
            "geometry": {
                "type": type,
                "coordinates": coord
            }
        }

        return feature;
    }


    async getLocationPaths() {
        let _storagePaths = JSON.parse(await AsyncStorage.getItem('path-' + Helper.currentTaskId));
        //console.log(_storagePaths, "_storagePaths");

        if (!_storagePaths) {
            _storagePaths = [];
        }

        return _storagePaths;
    }



    render() {

        return (
            <View style={styles.flexContainerMain}>
                <View style={styles.MapContainer}>
                    {/*                    <MapboxGL.MapView
                        ref={(c) => this._map = c}
                        style={styles.MapContainer}
                        styleURL={MapboxGL.StyleURL.Satellite}
                        showUserLocation={true}
                        logoEnabled={false}
                    >
                        <MapboxGL.Camera
                            ref={ref => this.camera = ref}
                            zoomLevel={14}
                            maxZoomLevel={20}
                            animationMode={'flyTo'}
                            animationDuration={6000}
                            centerCoordinate={this.state.defaultCenterCoords}
                        />

                        <MapboxGL.UserLocation
                            showsUserHeadingIndicator={true}
                        />

                    </MapboxGL.MapView> */}
                </View>

            </View>
        );
    }


    async onLocationChange() {
        console.warn("onLocationChange");

        globalLocationPaths = await instance.getLocationPaths();

        let lineCoords = instance.buildLineFeature(globalLocationPaths);


        instance.setState({
            GeoJSON: {
                type: "FeatureCollection",
                features: [instance.featureMaker("Recorrido de tareas", "LineString", lineCoords)]
            },
            lineCoords: lineCoords
        });


    }


    saveLocation = async (latitude, longitude) => {
        globalLocationPaths[0] = {
            init: true,
            creationDate: new Date(),
            coords: [
                longitude,
                latitude
            ],
        };

        let item = JSON.parse(await AsyncStorage.getItem('path-' + Helper.currentTaskId));

        if (!item) {
            await AsyncStorage.setItem('path-' + Helper.currentTaskId, JSON.stringify(globalLocationPaths));
        }
        //console.log('Ubicación detectada...')
        instance.setState({ gpsText: 'Ubicación detectada...' });

    }


}

export default MapaScreen;





