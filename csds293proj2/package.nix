{
  lib,
  jre,
  makeWrapper,
  maven,
}:
maven.buildMavenPackage rec {
  pname = "matchle";
  name = pname;
  version = "0.0.1";

  src = ./.;
  mvnHash = "sha256-kLpjMj05uC94/5vGMwMlFzLKNFOKeyNvq/vmB6pHTAo=";

  nativeBuildInputs = [makeWrapper];

  installPhase = ''
    mkdir -p $out/bin $out/share/${name}
    install -Dm644 ${name}/target/${name}.jar $out/share/${name}

    makeWrapper ${jre}/bin/java $out/bin/${name} \
      --add-flags "-jar $out/share/${name}/${name}.jar"
  '';

  meta = with lib; {
    description = "Matchle game java implementation";
    license = licenses.mit;
    maintainers = with maintainers; [_404wolf];
  };
}
