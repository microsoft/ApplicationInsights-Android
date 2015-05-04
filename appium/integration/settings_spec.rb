require File.expand_path('appium/spec_helper')

describe 'Settings' do
  describe 'About phone' do
    before do
      scroll_to('About phone').click
    end

    it "shows android version as numeric" do
      android_version = 'Android version'
      scroll_to(android_version)
      view = 'android.widget.TextView'
      version = xpath(%Q(//#{view}[preceding-sibling::#{view}[@text="#{android_version}"]])).text
      valid = !version.match(/\d/).nil?
      expect(valid).to eq(true)
    end
  end
end