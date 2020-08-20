File propertiesFile = new File( basedir, "bom-client/target/classes/test.properties" )

assert propertiesFile.isFile()

Properties properties = new Properties()
propertiesFile.withInputStream { 
    properties.load(it)
}

assert properties."test.property" == "test.value"
assert properties."included.property" == "foo"
assert properties."excluded.property" == "\${excluded.property}"