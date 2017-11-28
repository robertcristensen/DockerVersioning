package com.pipeline.libs

class DockerRegistry implements Serializable{
    private def steps     //object to pass current pipeline context
    private def regAddr   //registry address
    private def imgTag    //docker image tag
    private def imgRepo   //docker image repo name in registry
    private def connected //True if we were able to connect to registry API
    private def imgName   //full name
    private def branch    //branch name from image conventional tag
    DockerRegistry(steps){
        this.steps = steps
        this.connected = false
    }
    def connect(def img){
        // Parsing parameters from image object
        parseImg(img)
        // Test the connection to the regisrty API
        def RESULT
        this.regAddr = regAddr
        try { RESULT = execSh("curl -s -k -w \"%{http_code}\\n\" https://${this.regAddr}/v2/")
            if (RESULT[0].contains('200') && RESULT[1] == 0) {
                this.connected = true
                echo "Connection to registry ${this.regAddr} is SUCCESSFUL"
            }
        }
        catch (error) {
            echo "CONNECTION ERROR --- ERROR MESSAGE: "
            echo error.message
            this.connected = false
        }
    }
    private def getTags(String imgRepo = this.imgRepo){
        // Receive list of available tags for the image
        if (connected){
            return execSh("curl -k -s https://${this.regAddr}/v2/${imgRepo}/tags/list")[0]
        }
        else{return null}
    }
    private def getDigest(def tag=this.imgTag){
        // Receive digest number for the specified image tag
        if (connected){
            return execSh("curl -iks -H \"Accept: application/vnd.docker.distribution.manifest.v2+json\" " +
                    "https://${this.regAddr}/v2/${this.imgRepo}/manifests/${tag} | grep -iF Docker-Content-Digest |" +
                    "cut -d: --fields=2,3")[0]
        }
        else {return null}
    }
    private def getTimeCreated(def tag=this.imgTag){
        // Get image creation time (not work yet)
        def RESULT
        if (connected){
            RESULT = execSh("curl -ks https://${this.regAddr}/v2/${this.imgRepo}/manifests/${tag}")[0]
            echo RESULT
        }
        else {return null}
    }
    def deleteImage(def tag=this.imgTag){
        // Delete image by the specified tag
        def digest = getDigest(tag)
        def RESULT
        if (connected){
            try{
                RESULT = execSh("curl -iks -X \"DELETE\" https://${this.regAddr}/v2/${this.imgRepo}/manifests/${digest}")
                if (RESULT[0].contains('202') && RESULT[1] == 0){
                    echo "Image tagged ${tag} deleted SUCCESSFULY!"
                }
                else {
                    echo "!!!!!!!!!COULD NOT DELETE IMAGE. Image tag is ${tag}"
                    echo RESULT[0]
                }
            }
            catch (error){
                echo "!!!!!!!!!COULD NOT DELETE IMAGE"
                echo error.message
            }
        }
        else {return null}
    }
    private Tuple2 execSh(String script){
        // Execute sh script in current context
        def CODE
        def MESSAGE
        try { MESSAGE = steps.sh (script: script,
                returnStdout: true)
            CODE = 0
        } catch (error) {
            echo error.message
            CODE = -1
        } finally {
            return new Tuple2(MESSAGE.trim(), CODE)
        }
    }
    private def parseImg(def img){
        def imgRepo
        this.imgName = img.imageName()
        def list = img.imageName().split('/')
        //get Registry address as first element
        def regAddr = list[0]
        //get image tag
        def imgTag = list[list.size()-1].split(':')[1]
        // get branch name
        if (tagIsConventional(imgTag)){
            this.branch = imgTag.split('_')[1]
        }
        else {
            echo "Tag is not conventional: " + imgTag
            this.branch = ''
        }
        //get imgage repo name
        list[list.size()-1] = list[list.size()-1].split(':')[0]
        list = list[1..list.size()-1]
        imgRepo = list.join('/')
        this.regAddr = regAddr
        this.imgTag = imgTag
        this.imgRepo = imgRepo
        echo "Registry to purge images: " + regAddr
        echo "Input image tag: " + imgTag
        echo "Input image repo: " + imgRepo
    }
    private echo(String str){
        this.steps.echo str
    }
    private tagIsConventional(String str){
        if (str.split('_').size() == 3){return true}
        else {return false}
    }
    def purge(int n){
        // Purge images tagged for current branch if number gr then n
        if (!connected) {
            echo "Not connected to Registry API"
            return null
        }
        if (!tagIsConventional(imgTag)) {
            echo "Tag should be in following format:"
            echo "<version-from-root-pom>_<branch_name>_<build_number>"
            echo "Example: 900-SNAPSHOT_default_239"
            return null
        }
        else {
            echo "Current branch name is: " + branch
            def branch_tags = [:]
            def data = steps.readJSON text: getTags()
            def taglist = data["tags"]
            for(def i=0; i<taglist.size();i++){
                if( tagIsConventional(taglist[i]) && taglist[i].split('_')[1] == branch){
                    branch_tags[taglist[i]] = taglist[i].split('_')[2].toInteger()
                }
            }
            while (branch_tags.size()>n){
                def min_element = branch_tags.min {it.key}
                deleteImage(branch_tags.min {it.key})
                branch_tags.remove(min_element)
            }
        }
    }
}
return this