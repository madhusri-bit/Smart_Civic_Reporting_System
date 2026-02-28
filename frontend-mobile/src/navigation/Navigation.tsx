// src/navigation/Navigation.jsx
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useColorScheme } from 'react-native';

import LandingScreen from '../screens/LandingScreen';
import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';

export type RootStackParamList = {
  Landing: undefined;
  Login: undefined;
  Register: undefined;
  ReportIssue: undefined;
  TrackIssues: undefined;
  IssueDetails: { issueId: string };
  Profile: undefined;
  Notifications: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const Navigation = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const screenOptions = {
    headerShown: false,
    animation: 'slide_from_right' as const,
    contentStyle: {
      backgroundColor: isDarkMode ? '#000000' : '#FFFFFF',
    },
  };

  return (
    <NavigationContainer>
      <Stack.Navigator 
        initialRouteName="Landing"
        screenOptions={screenOptions}
      >
        <Stack.Screen 
          name="Landing" 
          component={LandingScreen}
          options={{ animation: 'fade' }}
        />
        <Stack.Screen name="Login" component={LoginScreen} />
        <Stack.Screen name="Register" component={RegisterScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default Navigation;