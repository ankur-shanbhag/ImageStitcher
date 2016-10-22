set terminal push
set terminal pngcairo
set output outputfile

rgb(r,g,b) = 65536 * int(r) + 256 * int(g) + int(b)
splot datafile using 1:2:3:(rgb($4,$5,$6))  with points pt 4 ps 1 lc rgb variable

set terminal pop
set output
replot

pause -1