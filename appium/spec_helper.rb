require 'rubygems'
require 'rspec'
require 'rspec/expectations'
require 'appium_lib'

RSpec.configure do |config|
  config.before(:all) do
    options = {
      caps: {
        platformName: 'Android',
        app:'../app-sample/build/outputs/apk/app-sample-debug.apk',
        deviceName: 'appinsights-appium' #required but doesn't have to match for genymotion  or android emu
      },
      launchTimeout: 5000
    }

    driver = Appium::Driver.new(options).start_driver
    driver.manage.timeouts.implicit_wait = 10
    Appium.promote_appium_methods Object
  end

  config.after(:all) do
    driver_quit
  end
end