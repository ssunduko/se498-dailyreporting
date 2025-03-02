package com.se498.dailyreporting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GraphQLToolsController {

    @GetMapping(value = "/voyager", produces = "text/html")
    @ResponseBody
    public String voyager() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>GraphQL Voyager</title>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <!-- Load React dependencies -->
                <script src="https://cdn.jsdelivr.net/npm/react@16/umd/react.production.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/react-dom@16/umd/react-dom.production.min.js"></script>
                
                <!-- Load GraphQL Voyager -->
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/graphql-voyager@1.0.0-rc.31/dist/voyager.css" />
                <script src="https://cdn.jsdelivr.net/npm/graphql-voyager@1.0.0-rc.31/dist/voyager.min.js"></script>
                
                <style>
                    body {
                        height: 100%;
                        margin: 0;
                        width: 100%;
                        overflow: hidden;
                    }
                    #voyager {
                        height: 100vh;
                    }
                </style>
            </head>
            <body>
                <div id="voyager">Loading GraphQL Voyager...</div>
                
                <script>
                    // Wait until the page and all scripts are fully loaded
                    document.addEventListener('DOMContentLoaded', function() {
                        function introspectionProvider(query) {
                            return fetch('/graphql', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Accept': 'application/json',
                                },
                                body: JSON.stringify({ query: query })
                            }).then(response => response.json());
                        }
                        
                        // Access Voyager through the global namespace where it's attached
                        const { Voyager } = window.GraphQLVoyager;
                        
                        // Render Voyager
                        ReactDOM.render(
                            React.createElement(Voyager, {
                                introspection: introspectionProvider,
                                workerURI: 'https://cdn.jsdelivr.net/npm/graphql-voyager@1.0.0-rc.31/dist/voyager.worker.js',
                                displayOptions: {
                                    skipRelay: true,
                                    skipDeprecated: false,
                                    sortByAlphabet: true,
                                    showLeafFields: true,
                                    hideRoot: false
                                }
                            }),
                            document.getElementById('voyager')
                        );
                    });
                </script>
            </body>
            </html>
            """;
    }

    @GetMapping(value = "/playground", produces = "text/html")
    @ResponseBody
    public String playground() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>GraphQL Playground</title>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link href="https://cdn.jsdelivr.net/npm/graphql-playground-react@1.7.26/build/static/css/index.css" rel="stylesheet">
                    <script src="https://cdn.jsdelivr.net/npm/graphql-playground-react@1.7.26/build/static/js/middleware.js"></script>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            height: 100vh;
                            width: 100vw;
                        }
                    </style>
                </head>
                <body>
                    <div id="root"></div>
                    <script>
                        window.addEventListener('load', function (event) {
                            GraphQLPlayground.init(document.getElementById('root'), {
                                endpoint: '/graphql',
                                settings: {
                                    'editor.theme': 'light',
                                    'editor.fontSize': 14,
                                    'editor.fontFamily': "'Fira Code', 'Source Code Pro', monospace",
                                    'tracing.hideTracingResponse': false
                                }
                            });
                        });
                    </script>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/altair", produces = "text/html")
    @ResponseBody
    public String altairEmbed() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Altair GraphQL Client (Embedded)</title>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        height: 100vh;
                        width: 100vw;
                        overflow: hidden;
                    }
                    iframe {
                        border: none;
                        width: 100%;
                        height: 100vh;
                    }
                </style>
            </head>
            <body>
                <!-- Using the Altair public deployment as a direct embed -->
                <iframe src="https://altair-gql.sirmuel.design/?endpoint=http://localhost:8080/graphql"></iframe>
            </body>
            </html>
            """;
    }
}