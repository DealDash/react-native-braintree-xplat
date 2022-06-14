require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'RCTBraintree'
  s.version = package['version']
  s.summary = package['description']
  s.description = package['description']
  s.homepage = 'https://github.com/DealDash/react-native-braintree-xplat.git'
  s.license = package['license']
  s.author = { 'Nick Koutrelakos' => 'nick@stadiumgoods.com' }
  s.platform = :ios, '11.0'
  s.source = { :git => 'https://github.com/DealDash/react-native-braintree-xplat.git', :tag => 'master' }
  s.source_files = 'ios/**/*.{h,m}'
  s.requires_arc = true

  s.ios.deployment_target = '11.0'
  s.tvos.deployment_target = '11.0'

  s.dependency 'Braintree', '4.32.1'
  s.dependency 'BraintreeDropIn', '8.1.3'
  s.dependency 'Braintree/PayPal'
  s.dependency 'Braintree/Apple-Pay', '4.32.1'
  s.dependency 'Braintree/PaymentFlow'
  s.dependency 'React'
end