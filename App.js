import React from 'react';
import { AppRegistry, DeviceEventEmitter } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Helper } from './managers/Helper';

//react-navigation 5
import { NavigationContainer } from "@react-navigation/native";
import { createStackNavigator } from '@react-navigation/stack';
//import { createDrawerNavigator } from '@react-navigation/drawer'; 
//import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
//import { createMaterialTopTabNavigator } from '@react-navigation/m0aterial-top-tabs';

//MapboxGL
//import MapboxGL from '@react-native-mapbox-gl/maps'

//MapboxGL Token
//MapboxGL.setAccessToken("pk.eyJ1IjoidGljc2RldmVsb3BlciIsImEiOiJja3lzdXFyN28xN29pMnBsZTl6ejZhZ3RqIn0.R9yqgrZtvoARVA2CeqJ8hQ");


//importando pantallas. 
import HomeScreen from './pages/home';
import MapaScreen from './pages/map';


const AppStack = createStackNavigator();

function AppStackScreen() {
  return (
    <AppStack.Navigator
      initialRouteName="Home"
    >
      <AppStack.Screen name="Home" component={HomeScreen} />
      <AppStack.Screen name="Mapa" component={MapaScreen} />
    </AppStack.Navigator>
  );
}


let globalLocationPaths = [];

const LogLocation = async (data) => {
  console.warn("headless pos LogLocation ", data);

  if (data.speed > 1.7) {
    // console.warn('evitando velocidad', data.speed);
    return;
  }

  try {
    if (Helper.currentTaskId != "" && Helper.positionDetected) {
      globalLocationPaths = JSON.parse(await AsyncStorage.getItem('path-' + Helper.currentTaskId));

      if (globalLocationPaths) {
        if (globalLocationPaths[0].init === undefined) {
          globalLocationPaths[0].coords = [
            data.lng,
            data.lat
          ]

          globalLocationPaths[0].init = false;

        }
        else {
          //ToastAndroid.show("Agregar al array", 2000);
          console.warn("Agregar al array");
          globalLocationPaths.push({
            creationDate: new Date(),
            coords: [
              data.lng,
              data.lat
            ]
          });

        }

      }


      await AsyncStorage.setItem('path-' + Helper.currentTaskId, JSON.stringify(globalLocationPaths));

      if (data.appState == "foreground") {
        DeviceEventEmitter.emit('onLocationChange', {});
      }
    }
  } catch (error) {
    console.warn("Error", error)
    //ToastAndroid.show("Error\n: "+error, 5000);
  }

}

AppRegistry.registerHeadlessTask('LogLocation', () => LogLocation);


class App extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      appSession: undefined,
      isOpen: false
    }

    instance = this;

    // Ignore yellow box in DEV mode
    /*  console.ignoredYellowBox = true;
        console.disableYellowBox = true;
        console.reportErrorsAsExceptions = false;  */
  }

  async UNSAFE_componentWillMount() {
    //console.log("connectionStatusModule", connectionStatusModule)
  }


  render() {
    return (
      <NavigationContainer>
        <AppStackScreen />
      </NavigationContainer>
    )
  }

}

export default App;

