{ pkgs, jvmMemorySize ? "-Xmx4000m", ... }:
  pkgs.stdenv.mkDerivation rec {
    pname = "mixnet";
    version = "9.0.0";
    src = ./.;

    nativeBuildInputs = [
      pkgs.python3Minimal
      pkgs.gnum4
      pkgs.automake
      pkgs.makeWrapper
      pkgs.patchelf
      pkgs.autoconf
      pkgs.jdk8
    ];
    buildInputs = [
      pkgs.jre8
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
      echo "edu-install-phase: start"
      mkdir -p $out/bin
      mkdir -p $out/share/java
      mkdir -p $out/lib
      find mixnet/bin -type f -not -name '*.src' -exec cp {} $out/bin \;
      find . -name "*.jar" -exec cp {} $out/share/java \;
      find . -name "*.so*" -exec cp {} $out/lib \;
      patchelf --remove-rpath $out/lib/*.so*;
      echo "edu-install-phase: stop"
    '';
    # TODO:
    # - convert patchelf into a find -exec
    # - add a find -exec wrapProgram to bins
    # - nix flake update in eo
    
  }