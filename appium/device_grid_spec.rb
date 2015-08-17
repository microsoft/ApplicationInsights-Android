require 'rubygems'
require 'rspec'
require 'rspec/expectations'
require 'appium_lib'
require 'securerandom'

  def defaultTestRun
  it 'should should tap track 5 times' do
    list_el = text('Track event')
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
  end

it 'should background for 3s and foreground again 3 times' do 
  background_app(3)
  sleep(2)
  background_app(3)
  sleep(2)
  background_app(3)
end

it 'should should tap track 5 times' do
    list_el = text('Track event')
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
    list_el.click
    back
  end

  it 'should background for 10s and foreground again' do 
  background_app(10)
  sleep(2)
end

  it 'should trigger a sync' do
    list_el = text('Trigger Synchronize')
    list_el.click 
    back
    sleep(5)
    list_el.click
    back
  end

#end of default test run
end


RSpec.describe 'Run on ODL' do  
before(:all) do
    randomId = SecureRandom.uuid
    options = {
      caps: {
        #Appium capabilities for android app
        #Add
        app: 'URL TO APK',
        appPackage: 'com.microsoft.applicationinsights.sampleapp',
        appActivity: 'com.microsoft.applicationinsights.appsample.ItemListActivity',
        platform: 'WINDOWS',  
        deviceName: 'android',
        platformName: 'android',
        #Device Lab capabilities
        olympusTeam: 'BingExperience',
        olympusSessionId: randomId,
        launchTimeout: 5000
      },
      appium_lib: {server_url: 'URL OF DEVICE LAB'}
    }  
  # appium specific driver with helpers available
  @driver = Appium::Driver.new(options).start_driver
  @driver.manage.timeouts.implicit_wait = 10
  Appium.promote_appium_methods Object
  end
  
  after(:all) do
    if(@driver != nil) 
          @driver.driver_quit
    end
  end

#begin of actual tests
describe 'Run default tests' do
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end
end

describe 'restart' do
      it 'should restart the driver after a crash' do
        restart
        sleep(3)
      end
    end

describe 'Run with disabled session management' do
  it 'Can disable session management' do
 list_el = text('Disable session management')
  list_el.click
  back
end
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end
end

describe 'restart' do
      it 'should restart the driver after a crash' do
        restart
        sleep(3)
      end
    end

describe 'Run with re-enabled session management' do
  it 'Can enable session management' do
 list_el = text('Enable session management')
  list_el.click
  back
end
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end
end

describe 'restart' do
      it 'should restart the driver after a crash' do
        restart
        sleep(3)
      end
    end

describe 'Run with disabled pageviews' do
  it 'Can disable pageviews' do
 list_el = text('Disable page view tracking')
  list_el.click
  back
end
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end
end

describe 'restart' do
      it 'should restart the driver after a crash' do
        restart
        sleep(3)
      end
    end

describe 'Run with re-enabled pageviews' do
  it 'Can re-enable pageviews' do
 list_el = text('Enable page view tracking')
  list_el.click
  back
end
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end
end

describe 'restart' do
      it 'should restart the driver after a crash' do
        restart
        sleep(3)
      end
    end

describe 'Run with disabled pageviews and session management' do
  it 'Can disable pageviews' do
 list_el = text('Disable page view tracking')
  list_el.click
  back
end
 it 'Can disable session management' do
 list_el = text('Disable session management')
  list_el.click
  back
end
defaultTestRun 
end
  
end