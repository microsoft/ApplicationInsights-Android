require File.expand_path('spec_helper')

describe 'When we open our sample app' do
  #describe 'Tap track event' do
    #before do
      #scroll_to('Track event')
    #end

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

it 'should background and foreground the app 3 times' do 
  background_app(3)
  sleep(2)
  background_app(3)
  sleep(2)
  background_app(3)
end

  it 'should trigger a sync' do
    list_el = text('Trigger Synchronize')
    list_el.click 
    back
    sleep(5)
    list_el.click
    back
  end


it 'should crash the app' do
  list_el = text('Crash the App!')
  list_el.click
end

end