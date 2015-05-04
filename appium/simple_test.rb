# GETTING STARTED
# -----------------
# This documentation is intended to show you how to get started with a
# simple Appium & appium_lib test.  This example is written without a specific
# testing framework in mind;  You can use appium_lib on any framework you like.
#
# INSTALLING RVM
# --------------
# If you don't have rvm installed, run the following terminal command
#
# \curl -L https://get.rvm.io | bash -s stable --ruby
#
# INSTALLING GEMS
# ---------------
# Then, change to the example directory:
#   cd appium-location/sample-code/examples/ruby
#
# and install the required gems with bundler by doing:
#   bundle install
#
# RUNNING THE TESTS
# -----------------
# To run the tests, make sure appium is running in another terminal
# window, then from the same window you used for the above commands, type
#
# bundle exec ruby simple_test.rb
#
# It will take a while, but once it's done you should get nothing but a line
# telling you "Tests Succeeded";  You'll see the iOS Simulator cranking away
# doing actions while we're running.
require 'rubygems'
require 'appium_lib'
require 'test/unit'

class SettingsTest < Test::Unit::TestCase
  def setup
    caps   = { caps:     { platformName: 'Android',
                             deviceName: 'appinsights-appium',
                             appActivity: '.ItemListActivity',
                             appPackage: 'com.microsoft.applicationinsights.appsample '} }
    driver = Appium::Driver.new(caps)
    #Appium.promote_appium_methods self.class
    driver.start_driver.manage.timeouts.implicit_wait = 20 # seconds
    driver.start_activity(".ItemListActivity")
  end

def teardown
    driver_quit
end

def test_about_phone_version
    # This may be 'About phone' or 'About tablet'
    # search for About to work on both phones & tablets.
    scroll_to('Track Event ').click
  end

end
