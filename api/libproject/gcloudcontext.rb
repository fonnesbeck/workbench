require_relative "../../libproject/utils/common"
require "json"

class GcloudContextV2
  attr_reader :account, :project

  def initialize(options_parser)
    # We use both gcloud and gsutil commands for various tasks. While gcloud can take arguments,
    # gsutil uses the current gcloud config, so we want to grab and verify the account from there.
    # We do NOT grab the project from gcloud config since that can easily be set to something
    # dangerous, like a production project. Instead, we always require that parameter explicitly.
    common = Common.new
    options_parser.add_option(
      "--project [GOOGLE_PROJECT]",
      lambda {|opts, v| opts.project = v},
      "Google project to act on (e.g. all-of-us-workbench-test)"
    )
    options_parser.add_validator lambda {|opts| raise ArgumentError unless opts.project}
    options_parser.parse.validate
    @project = options_parser.opts.project
    common.status "Reading glcoud configuration..."
    configs = common.capture_stdout \
        %W{docker-compose run --rm api gcloud --format=json config configurations list}
    active_config = JSON.parse(configs).select{|x| x["is_active"]}.first
    common.status "Using '#{active_config["name"]}' gcloud configuration"
    @account = active_config["properties"]["core"]["account"]
    common.status "  account: #{@account}"
    unless @account
      common.error "Account must be set in gcloud config. Try:\n" \
          "  gcloud auth login your.name@pmi-ops.org"
      exit 1
    end
    unless @account.end_with?("@pmi-ops.org")
      common.error "Account is not a pmi-ops.org account: #{@account}. Try:\n" \
          "  gcloud auth login your.name@pmi-ops.org"
      exit 1
    end
  end

  def ensure_service_account()
    sa_key_path = "src/main/webapp/WEB-INF/sa-key.json"
    unless File.exist? sa_key_path
      Common.new.run_inline %W{
        docker-compose run --rm api gsutil cp
          gs://#{@project}-credentials/all-of-us-workbench-test-9b5c623a838e.json
          #{sa_key_path}
      }
    end
  end
end