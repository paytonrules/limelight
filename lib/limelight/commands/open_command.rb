#- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require 'limelight/commands/command'

module Limelight
  module Commands

    # See the following usage summary.
    #
    #    Usage: limelight open <production_path>
    #        Open a limelight production.
    #        options:
    #        -h, --help                       Prints this usage summary.
    #
    class OpenCommand < Command

      install_as "open"

      DEFAULT_PRODUCTION = File.expand_path($LIMELIGHT_HOME + "/productions/startup")

      attr_reader :drb_port

      def initialize
        self.print_backtrace = true
      end

      def self.description
        return "Open a limelight production."
      end

      protected ###########################################

      def parameter_description
        return "<production_path>"
      end

      def do_requires
        Main.initialize_context
        require 'limelight/producer'
      end

      def build_options(spec) #:nodoc:
        spec.on("-d <port>", "--drb_port=<port>", "Publish the opened production using DRb on the specified port.") { |value| @drb_port = value }
      end

      def parse_remainder(args)
        @production_path = args.shift || DEFAULT_PRODUCTION
      end

      def process
        Studio.publish_on_drb(@drb_port.to_i) if @drb_port
        Studio.open(@production_path)
      end

    end
    
  end
end