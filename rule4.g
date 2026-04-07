struct myStruct {
int[][] x;
int y;
}
// Would Type Check
var myStruct s = {{{1,2},{1,2}},1};
// Would not Type Check
myStruct s = {1,2};