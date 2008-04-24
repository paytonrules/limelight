require File.expand_path(File.dirname(__FILE__) + "/spec_helper")
require 'limelight/prop_builder'

describe Limelight::SceneBuilder do

  before(:each) do
    @caster = make_mock("caster", :fill_cast => nil)
    @options = { :name => "root", :casting_director => @caster}
  end
  
  it "should build root" do
    root = Limelight.build_scene(@options)
    
    root.class.should == Limelight::Scene
    root.name.should == "root"
    root.panel.should_not == nil
    root.children.size.should == 0
  end
  
  it "should build one child prop" do
    root = Limelight::build_scene(@options) do
      child
    end
    
    root.children.size.should == 1
    child = root.children[0]
    child.class.should == Limelight::Prop
    child.name.should == "child"
    child.panel.should_not == nil
    child.children.size.should == 0
  end
  
  it "should allow multiple children" do
    root = Limelight::build_scene(@options) do
      child1
      child2
    end
    
    root.children.size.should == 2
    root.children[0].name.should == "child1"
    root.children[1].name.should == "child2"
  end

  it "should allow nested children" do
    root = Limelight::build_scene(@options) do
      child do
        grandchild
      end
    end

    root.children.size.should == 1
    root.children[0].name.should == "child"
    root.children[0].children.size.should == 1
    root.children[0].children[0].name.should == "grandchild"
  end
  
  it "should be able to set the id" do
    root = Limelight::build_scene(@options) do
      child :id => "child_1", :players => "x, y, z"
    end
    
    child = root.children[0]
    child.id.should == "child_1"
    child.players.should == "x, y, z"
  end
  
  it "should allow setting styles" do
    root = Limelight::build_scene(@options) do
      child :width => "100", :font_size => "10", :top_border_color => "blue"
    end
    
    child = root.children[0]
    child.style.width.should == "100"
    child.style.font_size.should == "10"
    child.style.top_border_color.should == "blue"
  end
  
  it "should allow defining events through constructor" do
    root = Limelight::build_scene(@options) do
      child :on_mouse_entered => "return [self, event]"
    end  
    
    child = root.children[0]
    child.mouse_entered("blah").should == [child, "blah"]
  end
  
  it "should allow scene configuration" do
    root = Limelight::build_scene(@options) do
      __ :name => "root", :id => "123"
    end
    
    root.children.size.should == 0
    root.name.should == "root"
    root.id.should == "123"
  end
  
  it "should give every prop their scene" do
    root = Limelight::build_scene(@options) do
      child do
        grandchild
      end
    end
    
    root.scene.should == root
    root.children[0].scene.should == root
    root.children[0].children[0].scene.should == root
  end

  it "should install external props" do
    loader = make_mock("loader", :exists? => true)
    loader.should_receive(:load).with("external.rb").and_return("child :id => 123")
    
    root = Limelight::build_scene(:id => 321, :build_loader => loader, :casting_director => @caster) do
      __install "external.rb"
    end  
    
    root.id.should == 321
    root.children.size.should == 1
    child = root.children[0]
    child.name.should == "child"
    child.id.should == 123
  end
  
  it "should fail if no loader is provided" do
    begin
      root = Limelight::build_scene(@options.merge(:id => 321, :build_loader => nil)) do
        __install "external.rb"
      end
      root.should == nil # should never get here
    rescue Exception => e
      e.message.should == "Cannot install external props because no loader was provided"
    end
  end
  
  it "should fail when the external file doesn't exist" do
    loader = make_mock("loader")
    loader.should_receive(:exists?).with("external.rb").and_return(false)
    
    begin
      root = Limelight::build_scene(@options.merge(:id => 321, :build_loader => loader)) do
        __install "external.rb"
      end
    rescue Exception => e
      e.message.should == "External prop file: 'external.rb' doesn't exist"
    end
  end
  
  it "should fail with PropException when there's problem in the external file" do
    loader = make_mock("loader", :exists? => true)
    loader.should_receive(:load).with("external.rb").and_return("+")
    
    begin
      root = Limelight::build_scene(@options.merge(:id => 321, :build_loader => loader)) do
        __install "external.rb"
      end  
    rescue Limelight::BuildException => e
      e.message.should include("external.rb:1: (eval):1: , unexpected end-of-file")
    end
  end
  
  it "should build onto an existing block" do
    prop = Limelight::Prop.new
    prop.set_scene(Limelight::Scene.new(:casting_director => make_mock(:casting_director, :fill_cast => nil)))
    builder = Limelight::PropBuilder.new(prop)
    block = Proc.new { one; two { three } }
    builder.instance_eval(&block)
    
    prop.children.length.should == 2
    prop.children[0].name.should == "one"
    prop.children[1].name.should == "two"
    prop.children[1].children.length.should == 1
    prop.children[1].children[0].name.should == "three"
  end
  
end