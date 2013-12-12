#!/usr/bin/env ruby

require "ftools"

theme_name = ARGV[0]
res = ARGV[1]
output_dir = ARGV[2]
base_dir = Dir.pwd
Dir.chdir(res)

# Rename and move images to correct path and under the correct theme name
Dir.glob(['**/*.png', '**/*.xml']).each do |name| 
   directory = File.dirname(name)
   filename = File.basename(name)
   result_dir = base_dir + '/' + output_dir + '/' + directory + '/' + theme_name + '__' + filename 
   src_dir = base_dir + '/' + res + name
   puts src_dir, result_dir

   if File.extname(filename) == ".xml"
      source_text = File.read(src_dir)
      source_text = source_text.gsub(/(@drawable\/)(\w.*\b)/, '\1' + theme_name + "__" + '\2')
      File.open(src_dir, 'w') { |f| f.write(source_text) }
   end

   # Copy the file locally to a temp directory to do string substitution
   File.copy(src_dir, result_dir)
end

