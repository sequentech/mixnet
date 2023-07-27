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

            nativeBuildInputs = [ pkgs.makeWrapper pkgs.autotools ];
            buildInputs = [ pkgs.jdk8 ];

            configureFlags = []; # [ "--enable-jgmpmee" "--enable-jecn" ];

            makeFlags = [ "JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8" ];

            installPhase = ''
              ls -lah $out
              mkdir -p $out/bin
              cp -r . $out/bin
              wrapProgram $out/bin/mixnet \
                --set JAVA_TOOL_OPTIONS "-Dfile.encoding=UTF8"
            '';

            fixupPhase = ''
              find $out/bin -type f -executable -exec patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" {} \;
            '';
          };
        in {
          packages.default = mixnet;
        };
        flake = {};
    };
}
