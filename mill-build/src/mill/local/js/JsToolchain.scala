package mill.local.js

trait JsToolchain {
    def name:String
    def primaryRunner:String 
    def packageManager:String 
    def packageManagerRunner:String         
}