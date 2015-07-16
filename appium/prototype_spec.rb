require 'rubygems'
require 'rspec'
require 'rspec/expectations'
require 'appium_lib'

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


RSpec.describe 'ODL' do
  before(:all) do
    options = {
      caps: {
        platformName: 'Android',
        app:'../app-sample/build/outputs/apk/app-sample-debug.apk',
        deviceName: 'appinsights-appium' #required but doesn't have to match for genymotion  or android emu
      },
      launchTimeout: 5000
    }

    @driver = Appium::Driver.new(options).start_driver
    @driver.manage.timeouts.implicit_wait = 10
    Appium.promote_appium_methods Object
  end

  after(:all) do
    @driver.driver_quit
  end

describe 'Run default tests' do
  defaultTestRun
  it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
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

