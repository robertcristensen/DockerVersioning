#!/usr/bin/env groovy
import com.pipeline.libs.DockerRegistry

def call(body){

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def r = new DockerRegistry(this)
    r.connect('bsregistrydocker.emea.int.genesyslab.com/genesys/cx-widget:default')

    node{
        stage ('blabla'){
            echo "hello from library"
        }
    }
}

