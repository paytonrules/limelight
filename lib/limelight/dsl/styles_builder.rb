#- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require 'limelight/util'

module Limelight

  # A trigger to define Style objects using the StyleBuilder DSL.
  #
  # See Limelight::Stylesbuilder, Limelight::Stylebuilder
  #
  def self.build_styles(style_hash = nil, &block)
    builder = DSL::StylesBuilder.new(style_hash)
    builder.instance_eval(&block) if block
    return builder.__styles__
  end

  module DSL

    # The basis of the DSL for building Style objects.
    #
    # Sample StyleBuilder DSL
    #
    #  sandbox {
    #    width "100%"
    #    height "100%"
    #    vertical_alignment :top
    #  }
    #
    #  sample {
    #    width 320
    #    height 320
    #    gradient :on
    #  }
    #
    #  spinner {
    #    extends :sample
    #    background_color :green
    #    secondary_background_color :blue
    #    gradient_angle 0
    #    gradient_penetration 100
    #  }
    #
    # This exmple builds three styles: sandbox, sample, spinner.  Within each style block, the individual attributes of
    # the style may be set.
    #
    # See Limelight::Styles
    #
    class StylesBuilder

      Limelight::Util.lobotomize(self)

      attr_reader :__styles__

      def initialize(style_hash = nil)
        @__styles__ = style_hash || {}
      end

      def method_missing(sym, &block) #:nodoc:
        __add_style__(sym.to_s, &block)
      end

      def __add_style__(name, &block) #:nodoc:
        builder = StyleBuilder.new(name, self)
        builder.instance_eval(&block) if block
        @__styles__[name] = builder.__style__
      end
    end

    # The basis of the DSL for defining a Style object.
    #
    class StyleBuilder

      Limelight::Util.lobotomize(self)

      attr_reader :__style__  #:nodoc:

      def initialize(name, styles_builder, options = {})  #:nodoc:
        @__name = name
        @__styles_builder = styles_builder
        @__style__ = @__styles_builder.__styles__[name] || Styles::RichStyle.new
      end

      # Used to define a hover style.  Hover styles are appiled when the mouse passed over a prop using the specified style.
      #
      #   spinner {
      #     width 50
      #     height 50
      #     hover {
      #       text_color :white
      #     }
      #   }
      #
      # The text color of props using the 'spinner' style will become white when the mouse hovers over them.
      #
      def hover(&block)
        @__styles_builder.__add_style__("#{@__name}.hover", &block)
      end

      # Styles may extend other styles.
      #
      #    base {
      #      background_color :red
      #    }
      #
      #    cell {
      #      extends :base
      #      text_color :black
      #    }
      #
      # The 'cell' style now has all attributes defined in 'base'.  Therefore any prop using the 'cell' style
      # will have a red background.  Styles may override attributes aquired through extension.
      #
      def extends(*style_names)
        style_names.each do |style_name|
          extension = @__styles_builder.__styles__[style_name.to_s]
          raise StyleBuilderException.new("Can't extend missing style: '#{style_name}'") if extension.nil?
          @__style__.add_extension(extension)
        end
      end

      def method_missing(sym, value) #:nodoc:
        setter_sym = "#{sym}=".to_s
        raise StyleBuilderException.new("'#{sym}' is not a valid style") if !@__style__.respond_to?(setter_sym)
        @__style__.send(setter_sym, value.to_s)
      end
    end

    # Exception thrown by StyleBuilder when an error is encountered.
    #
    class StyleBuilderException < Exception
    end

  end
end