{
  description = "A simple flake for building and installing the mixnet.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };
  outputs = inputs@{ flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      imports = [];
      systems = [ "x86_64-linux" ];

      # Per-system attributes can be defined here. The self' and inputs'
      # module parameters provide easy access to attributes of the same
      # system.
      perSystem = { config, self', inputs', pkgs, system, lib, ... }:
        let
          mixnet = pkgs.stdenv.mkDerivation rec {
            pname = "mixnet";
            version = "9.0.0";
            src = ./.;

            nativeBuildInputs = [
              pkgs.makeWrapper
              pkgs.python3Minimal
              pkgs.gnum4
              pkgs.tree
            ];
            buildInputs = [
              pkgs.jdk8
              pkgs.gmp
            ];

            configureFlags = []; # [ "--enable-jgmpmee" "--enable-jecn" ];

            makeFlags = [ "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8" ];

            installPhase = ''
              mkdir -p $out/bin
              mkdir -p $out/share/java
              cp -r mixnet/bin/* $out/bin
              find . -name "*.jar" -exec cp {} $out/share/java \;
              tree $out
            '';
          };
        in {
          packages.default = mixnet;
        };
        flake = {};
    };
}
