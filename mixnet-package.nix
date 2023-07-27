{ pkgs, jvmMemorySize ? "-Xmx4000m", ... }:
  pkgs.stdenv.mkDerivation rec {
    pname = "mixnet";
    version = "9.0.0";
    src = ./.;

    nativeBuildInputs = [
      pkgs.python3Minimal
      pkgs.gnum4
      pkgs.automake
      pkgs.autoconf
    ];
    buildInputs = [
      pkgs.jdk8
      pkgs.gmp
    ];

    configureFlags = [
      "--enable-jgmpmee"
      "--enable-jecn"
    ];

    makeFlags = [
      "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8"
      "JVM_MEMORY_SIZE=${jvmMemorySize}"
    ];

    installPhase = ''
      mkdir -p $out/bin
      mkdir -p $out/share/java
      find mixnet/bin -type f -not -name '*.src' -exec cp {} $out/bin \;
      find . -name "*.jar" -exec cp {} $out/share/java \;
    '';
  }