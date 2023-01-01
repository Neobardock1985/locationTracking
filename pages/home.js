import React from 'react';
import { View, Text, Button, PermissionsAndroid } from 'react-native';

class HomeScreen extends React.Component {

    constructor(props) {
        super(props);
    }

    async UNSAFE_componentWillMount() {
        await this.requestDevicePermissions();
    }


    goToMap() {
        this.props.navigation.navigate('Mapa', {});
    }

    async requestDevicePermissions() {
        try {

            // Requesting location permission...
            const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
                {
                    'title': 'Location',
                    'message': 'Allow location permission.'
                }
            )

            this.setState({ permissionGranted: granted })
            if (granted === PermissionsAndroid.RESULTS.GRANTED) {
                //console.warn("You can use the GPS")
            } else {
                console.warn("GPS permission denied")
            }

        } catch (err) {
            console.warn("Error", err)
        }
    }

    render() {
        return (
            <View>
                <Button
                    title="Empezar"
                    onPress={this.goToMap.bind(this)}
                />
            </View>
        );
    }
}

export default HomeScreen;