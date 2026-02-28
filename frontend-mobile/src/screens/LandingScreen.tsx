import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, ScrollView, Dimensions, StatusBar } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Camera, MapPin, Clock, Bell, CheckCircle, Users, Star, Zap } from 'lucide-react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { RootStackParamList } from '../navigation/Navigation';

const { width, height } = Dimensions.get('window');

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Landing'>;

const LandingScreen = () => {
  const navigation = useNavigation<NavigationProp>();

  const features = [
    { icon: Camera, title: 'Snap & Report', description: 'Capture civic issues with your camera', color: '#FF6B6B' },
    { icon: MapPin, title: 'Geo-Tagging', description: 'Automatic location detection', color: '#4ECDC4' },
    { icon: Clock, title: 'Real-time Tracking', description: 'Track issue resolution status', color: '#45B7D1' },
    { icon: Bell, title: 'Instant Alerts', description: 'Get notified about updates', color: '#96CEB4' },
  ];

  const stats = [
    { number: '500+', label: 'Issues Resolved', icon: CheckCircle },
    { number: '50+', label: 'Active Volunteers', icon: Users },
    { number: '95%', label: 'Satisfaction Rate', icon: Star },
    { number: '24h', label: 'Avg Response', icon: Zap },
  ];

  return (
    <SafeAreaView style={styles.container} edges={[]}>
      <StatusBar barStyle="light-content" backgroundColor="transparent" translucent />
      
      <ScrollView showsVerticalScrollIndicator={false} bounces={false}>
        <View style={styles.headerContainer}>
          <Image source={require('../assets/civic.jpg')} style={styles.headerImage} resizeMode="cover" />
          <View style={styles.overlay} />
          
          <View style={styles.headerContent}>
            <View style={styles.logoContainer}>
              <MapPin size={40} color="#FFFFFF" />
              <Text style={styles.logoText}>CivicEye</Text>
            </View>
            
            <Text style={styles.welcomeText}>Make Your City Better</Text>
            <Text style={styles.subtitleText}>Report civic issues instantly with geo-tagging</Text>
          </View>
        </View>

        <View style={styles.contentContainer}>
          <View style={styles.featuresContainer}>
            <Text style={styles.sectionTitle}>How It Works</Text>
            <View style={styles.featuresGrid}>
              {features.map((feature, index) => {
                const IconComponent = feature.icon;
                return (
                  <View key={index} style={styles.featureCard}>
                    <View style={[styles.iconContainer, { backgroundColor: feature.color + '20' }]}>
                      <IconComponent size={32} color={feature.color} />
                    </View>
                    <Text style={styles.featureTitle}>{feature.title}</Text>
                    <Text style={styles.featureDescription}>{feature.description}</Text>
                  </View>
                );
              })}
            </View>
          </View>

          <View style={styles.statsContainer}>
            <Text style={styles.sectionTitle}>Our Impact</Text>
            <View style={styles.statsGrid}>
              {stats.map((stat, index) => {
                const IconComponent = stat.icon;
                return (
                  <View key={index} style={styles.statCard}>
                    <IconComponent size={28} color="#2C3E50" />
                    <Text style={styles.statNumber}>{stat.number}</Text>
                    <Text style={styles.statLabel}>{stat.label}</Text>
                  </View>
                );
              })}
            </View>
          </View>

          <View style={styles.ctaContainer}>
            <Text style={styles.ctaTitle}>Ready to make a difference?</Text>
            <Text style={styles.ctaDescription}>
              Join thousands of citizens reporting and tracking civic issues in real-time
            </Text>
            
            <View style={styles.buttonContainer}>
              <TouchableOpacity style={styles.primaryButton} onPress={() => navigation.navigate('ReportIssue')} activeOpacity={0.8}>
                <Camera size={24} color="#FFFFFF" />
                <Text style={styles.primaryButtonText}>Report an Issue</Text>
              </TouchableOpacity>

              <TouchableOpacity style={styles.secondaryButton} onPress={() => navigation.navigate('TrackIssues')} activeOpacity={0.8}>
                <MapPin size={24} color="#2C3E50" />
                <Text style={styles.secondaryButtonText}>Track Issues</Text>
              </TouchableOpacity>
            </View>
          </View>

          <View style={styles.authContainer}>
            <TouchableOpacity style={styles.loginButton} onPress={() => navigation.navigate('Login')}>
              <Text style={styles.loginText}>Already have an account? <Text style={styles.loginBold}>Sign In</Text></Text>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.registerButton} onPress={() => navigation.navigate('Register')}>
              <Text style={styles.registerText}>New here? <Text style={styles.registerBold}>Create Account</Text></Text>
            </TouchableOpacity>
          </View>

          <View style={styles.footer}>
            <View style={styles.footerLinks}>
              <TouchableOpacity><Text style={styles.footerLink}>About</Text></TouchableOpacity>
              <Text style={styles.footerDot}>•</Text>
              <TouchableOpacity><Text style={styles.footerLink}>Privacy</Text></TouchableOpacity>
              <Text style={styles.footerDot}>•</Text>
              <TouchableOpacity><Text style={styles.footerLink}>Terms</Text></TouchableOpacity>
            </View>
            <Text style={styles.copyright}>© 2024 CivicEye. All rights reserved.</Text>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#FFFFFF' },
  headerContainer: { height: height * 0.4, position: 'relative' },
  headerImage: { width: '100%', height: '100%' },
  overlay: { ...StyleSheet.absoluteFillObject, backgroundColor: 'rgba(0,0,0,0.4)' },
  headerContent: { position: 'absolute', bottom: 30, left: 20, right: 20 },
  logoContainer: { flexDirection: 'row', alignItems: 'center', marginBottom: 15 },
  logoText: { fontSize: 28, fontWeight: 'bold', color: '#FFFFFF', marginLeft: 10 },
  welcomeText: { fontSize: 32, fontWeight: 'bold', color: '#FFFFFF', marginBottom: 8 },
  subtitleText: { fontSize: 16, color: '#FFFFFF', opacity: 0.9 },
  contentContainer: { flex: 1, backgroundColor: '#FFFFFF', borderTopLeftRadius: 30, borderTopRightRadius: 30, marginTop: -20, paddingTop: 20, paddingHorizontal: 20 },
  featuresContainer: { marginBottom: 30 },
  sectionTitle: { fontSize: 24, fontWeight: 'bold', color: '#2C3E50', marginBottom: 20, marginTop: 10 },
  featuresGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' },
  featureCard: { width: '48%', backgroundColor: '#F8F9FA', borderRadius: 16, padding: 16, marginBottom: 15, alignItems: 'center', shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4, elevation: 3 },
  iconContainer: { width: 56, height: 56, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  featureTitle: { fontSize: 16, fontWeight: '600', color: '#2C3E50', marginBottom: 4, textAlign: 'center' },
  featureDescription: { fontSize: 12, color: '#7F8C8D', lineHeight: 16, textAlign: 'center' },
  statsContainer: { marginBottom: 30 },
  statsGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' },
  statCard: { width: '48%', backgroundColor: '#F8F9FA', borderRadius: 16, padding: 16, marginBottom: 15, alignItems: 'center', shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4, elevation: 3 },
  statNumber: { fontSize: 24, fontWeight: 'bold', color: '#2C3E50', marginVertical: 4 },
  statLabel: { fontSize: 12, color: '#7F8C8D', textAlign: 'center' },
  ctaContainer: { backgroundColor: '#2C3E50', borderRadius: 24, padding: 24, marginBottom: 30, alignItems: 'center' },
  ctaTitle: { fontSize: 24, fontWeight: 'bold', color: '#FFFFFF', marginBottom: 8, textAlign: 'center' },
  ctaDescription: { fontSize: 14, color: '#FFFFFF', opacity: 0.9, textAlign: 'center', marginBottom: 20 },
  buttonContainer: { width: '100%', flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  primaryButton: { flex: 1, backgroundColor: '#FF6B6B', borderRadius: 12, padding: 15, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', shadowColor: '#FF6B6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 5 },
  primaryButtonText: { color: '#FFFFFF', fontSize: 14, fontWeight: '600', marginLeft: 8, flexShrink: 1 },
  secondaryButton: { flex: 1, backgroundColor: '#FFFFFF', borderRadius: 12, padding: 15, flexDirection: 'row', alignItems: 'center', justifyContent: 'center' },
  secondaryButtonText: { color: '#2C3E50', fontSize: 14, fontWeight: '600', marginLeft: 8, flexShrink: 1 },
  authContainer: { marginBottom: 30 },
  loginButton: { padding: 15, alignItems: 'center' },
  loginText: { fontSize: 14, color: '#7F8C8D' },
  loginBold: { fontWeight: 'bold', color: '#2C3E50' },
  registerButton: { padding: 15, alignItems: 'center', backgroundColor: '#F8F9FA', borderRadius: 12 },
  registerText: { fontSize: 14, color: '#7F8C8D' },
  registerBold: { fontWeight: 'bold', color: '#2C3E50' },
  footer: { alignItems: 'center', marginBottom: 20 },
  footerLinks: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  footerLink: { fontSize: 12, color: '#7F8C8D', marginHorizontal: 8 },
  footerDot: { fontSize: 12, color: '#7F8C8D' },
  copyright: { fontSize: 11, color: '#BDC3C7' },
});

export default LandingScreen;
