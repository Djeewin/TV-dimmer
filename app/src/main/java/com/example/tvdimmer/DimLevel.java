package com.example.tvdimmer;
final class DimLevel {static float alpha(int level){return (10-Math.max(1,Math.min(10,level)))/9f*0.95f;}}
