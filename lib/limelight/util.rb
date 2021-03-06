#- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

module Limelight

  # Utility methods for Limelight
  #
  module Util

    # Returns true if the specified file is a directory and has the structure of a Scene.
    #
    def self.is_limelight_scene?(dir)
      return directory_contains_file(dir, "props.rb")
    end

    # Returns true if the specified file is a directory and has the structure of a Production.
    def self.is_limelight_production?(dir)
      return directory_contains_file(dir, "stages.rb")
    end

    # Returns true of the file is a directory containing an entry named file_name.
    #
    def self.directory_contains_file(file, file_name)
      if file.is_a? String
        return File.directory?(file) && File.exists?(File.join(file, file_name))
      else
        return file.isDirectory() && File.exists?(File.join(file.absolute_path, file_name))
      end
    end

    # Removed all methods from a class except instance_eval and methods starting with __.
    # This is used by the DSL Builder classes to minimize reserved keywords.
    #
    def self.lobotomize(klass)
      klass.methods.each do |method_name|
        unless method_name[0..1] == "__" || method_name == "instance_eval"
          begin
            klass.instance_eval "undef_method :#{method_name}"
          rescue Exception => e
          end
        end
      end
    end

  end

end

class Module

  def prop_reader(*symbols)
    symbols.each do |sym|
      define_method(sym) do
        return scene.find(sym.to_s)
#        value = instance_variable_get("@#{sym}")
#        if value.nil?
#          value = scene.find(sym.to_s)
#          instance_variable_set("@#{sym}", value) unless value.nil?
#        end
#        return value
      end
    end
  end

end