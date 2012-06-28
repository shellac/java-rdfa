ARGF.each_line do |line|
  print line.gsub(/\d\d\d\d.html/) { |m| m[0,5] + 'xhtml' }
end