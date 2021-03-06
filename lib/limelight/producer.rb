#- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require 'limelight/file_loader'
require 'limelight/dsl/prop_builder'
require 'limelight/dsl/styles_builder'
require 'limelight/dsl/stage_builder'
require 'limelight/dsl/production_builder'
require 'limelight/casting_director'
require 'limelight/stage'
require 'limelight/dsl/build_exception'
require 'limelight/theater'
require 'limelight/production'
require 'limelight/gems'
require 'limelight/util/downloader'
require 'limelight/version'

module Limelight

  # A Producer has the hefty responsibility of producing Productions.  Given a directory, it will load the neccessary
  # files and create all the neccessary objects to bring a Production to life.
  #
  # For directory structures, see Limelight::Main
  #
  class Producer

    # Creates a new Producer and has it open a Production by specified name.
    #
    def self.open(production_name, options={})
      producer = new(production_name)
      begin
        producer.open(options)
      rescue Exception => e
        puts e
        puts e.backtrace
      end
    end

    attr_reader :theater, :production
    attr_writer :builtin_styles

    # A Production name, or root directory, must be provided. If not Theater is provided, one will be created.
    # You may also provide an existing Production for which this Producer will interact.
    #
    def initialize(root_path, theater=nil, production=nil)
      if (root_path[-4..-1] == ".lll")
        url = IO.read(root_path).strip
        root_path = Util::Downloader.download(url)
      end
      if (root_path[-4..-1] == ".llp")
        root_path = unpack_production(root_path)
      end
      @production = production || Production.new(root_path)
      @theater = theater.nil? ? Theater.new : theater
      establish_production
    end

    # Returns true if the production's minimum_limelight_version is compatible with the current version.
    #
    def version_compatible?
      current_version = Limelight::Util::Version.new(Limelight::VERSION::STRING)
      required_version = Limelight::Util::Version.new(@production.minimum_limelight_version)
      return required_version.is_less_than_or_equal(current_version)
    end

    # Returns the CastingDirector for this Production.
    #
    def casting_director
      @casting_director = CastingDirector.new(@production.root) if not @casting_director
      return @casting_director
    end

    # Loads the Production without opening it.  The Production will be created into memory with all it's stages
    #
    def load(options = {})
      Gems.install_gems_in_production(@production)
      Kernel.load(@production.init_file) if ( !options[:ignore_init] &&  File.exists?(@production.init_file) )
      load_stages if File.exists?(@production.stages_file)
    end

    # Opens the Production.
    #
    def open(options = {})
      return unless version_compatible? || Studio.utilities_production.proceed_with_incompatible_version?(@production.name, @production.minimum_limelight_version)
      @production.production_opening
      load
      @production.production_loaded
      if @theater.has_stages?
        @theater.stages.each do |stage|          
          open_scene(stage.default_scene.to_s, stage) if stage.default_scene
        end
      elsif @production.default_scene
        open_scene(@production.default_scene, @theater.default_stage)
      end
      @casting_director = nil
      @production.production_opened
    end

    # Opens the specified Scene onto the Spcified Stage.
    #
    def open_scene(name, stage, options={})
      path = @production.scene_directory(name)
      scene_name = File.basename(path)
      scene = load_props(options.merge(:production => @production, :casting_director => casting_director, :path => path, :name => scene_name))
      styles = load_styles(scene)
      merge_with_root_styles(styles)
      scene.styles = styles
      stage.open(scene)
      return scene
    end

    # Loads the 'stages.rb' file and configures all the Stages in the Production.
    #
    def load_stages
      stages_file = @production.stages_file
      content = IO.read(stages_file)
      stages = Limelight.build_stages(@theater) do
        begin
          eval content
        rescue Exception => e
          raise DSL::BuildException.new(stages_file, content, e)
        end
      end
      return stages
    end

    # Loads of the 'props.rb' file for a particular Scene and creates all the Prop objects and Scene.
    #
    def load_props(options = {})
      scene = Scene.new(options)
      if File.exists?(scene.props_file)
        content = IO.read(scene.props_file)
        options[:build_loader] = @production.root
        return Limelight.build_props(scene, options) do
          begin
            eval content
          rescue Exception => e
            raise DSL::BuildException.new(scene.props_file, content, e)
          end
        end
      else
        return scene
      end
    end

    # Loads the specified 'styles.rb' file and created a Hash of Styles.
    #
    def load_styles(context)
      styles = builtin_styles
      return styles if not File.exists?(context.styles_file)
      content = IO.read(context.styles_file)
      return Limelight.build_styles(styles) do
        begin
          eval content
        rescue Exception => e
          raise DSL::BuildException.new(context.styles_file, content, e)
        end
      end
    end

    # A production with multiple Scenes may have a 'styles.rb' file in the root directory.  This is called the
    # root_style.  This method loads the root_styles, if they haven't already been loaded, and returns them.
    #
    def root_styles
      return @root_syles if @root_syles   
      if File.exists?(@production.styles_file)
        @root_styles = load_styles(@production)
      else
        @root_styles = {}
      end
      return @root_styles
    end

    # Returns a hash of all the built-in Limglight Styles
    #
    def builtin_styles
      return @builtin_styles.dup if @builtin_styles
      builtin_styles_file = File.join($LIMELIGHT_LIB, "limelight", "builtin", "styles.rb")
      content = IO.read(builtin_styles_file)
      @builtin_styles = Limelight.build_styles do
        begin
          eval content
        rescue Exception => e
          raise BuildException.new(filename, content, e)
        end
      end
      return @builtin_styles.dup
    end

    def establish_production #:nodoc:
      @production.producer = self
      @production.theater = @theater

      production_file = @production.production_file
      if File.exists?(production_file)
        tmp_module = Module.new
        content = IO.read(production_file)
        tmp_module.module_eval(content, production_file)
        production_module = tmp_module.const_get("Production")
        raise "production.rb should define a module named 'Production'" if production_module.nil?
        @production.extend(production_module)
      end
    end

    private ###############################################

    def merge_with_root_styles(styles)
      root_styles.each_pair do |key, value|
        styles[key] = value if !styles.has_key?(key)
      end
    end

    def unpack_production(production_name)
      packer = Limelight::Util::Packer.new()
      dest_dir = File.join(Data.productions_dir, rand.to_s.sub("0.", ""))
      Dir.mkdir(dest_dir)
      return packer.unpack(production_name, dest_dir)
    end

  end

end