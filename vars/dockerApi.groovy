#!/usr/bin/env groovy
import com.pipeline.libs.DockerRegistry

def call(String name='haha'){
/*
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
*/
    echo "Hello from my lib ${haha}"

}

