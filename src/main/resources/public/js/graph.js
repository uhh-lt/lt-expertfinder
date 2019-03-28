function initColors() {
    /* COLORS & DESIGN */
    var colors = {};
    colors[1] = "#0174DF";
    colors[2] = "#04B404";
    colors[3] = "#DF7401";
    return colors;
}

function initMarkers() {
    // Per-type markers, as they don't inherit styles.
    d3.select('defs').selectAll("marker")
        .data(["mcollaboration", "mpublication", "mauthorship", "mcitation"])
        .enter().append("svg:marker")
        .attr("id", function(d) { return d;})
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 0)
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("markerUnits","userSpaceOnUse")
        .attr("orient", "auto-start-reverse")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
}

function initSvg(width, height) {
    var svg = d3.select("svg")
        .attr("width", width)
        .attr("height", height);

    var root = svg.append("svg:g");

    return {svg: svg, root: root};
}

function initForceLayout(initnodes, initlinks, width, height) {
    var force = d3.layout.force()
        .size([width, height])
        .nodes(initnodes)
        .links(initlinks)
        .gravity(.05)
        .distance(100)
        .charge(-200)
        .on("tick", tick);

    force.start();

    return force;
}

function initZoomBehahviour(svg) {
    var zoom = d3.behavior
        .zoom()
        .scaleExtent([1/4, 4])
        .on('zoom.zoom', function () {
            root.attr('transform',
                'translate(' + d3.event.translate + ')'
                +   'scale(' + d3.event.scale     + ')');
        })
    ;
    svg.call(zoom);
}

function initDragBehaviour(force) {
    /* DRAG */
    var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);

    function dragstart(d, i) {
        force.stop() // stops the force auto positioning before you start dragging
    }

    function dragmove(d, i) {
        d.px += d3.event.dx;
        d.py += d3.event.dy;
        d.x += d3.event.dx;
        d.y += d3.event.dy;
        tick(); // this is the key to make it work together with updating both px,py,x,y on d !
    }

    function dragend(d, i) {
        d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
        tick();
        force.resume();
    }

    return node_drag;
}

function initData() {
    var all_nodes = {};
    graph.nodes.forEach(function(node) {
        all_nodes[node.id] = node;
        all_nodes[node.id].links = 0;
        all_nodes[node.id].realgroup = node.group;
    });

    var all_links = graph.links;
    all_links.forEach(function(link) {
        link.weight = all_nodes[link.source].size + all_nodes[link.target].size;
        link.source = all_nodes[link.source];
        link.target = all_nodes[link.target];
    });

    // create node set from links
    var n = {};
    all_links.forEach(function(link) {
        n[link.source.id] = link.source;
        n[link.target.id] = link.target;
    });

    // fix link references, so that all links use same set
    all_links.forEach(function(link) {
        link.source = n[link.source.id];
        link.target = n[link.target.id];
    });

    all_links.sort(function(a, b) {
        return b.weight - a.weight;
    });

    var all_graph = buildGraph(all_links);
    return {all_nodes: n, all_links: all_links, all_graph: all_graph};
}

function buildGraph(graphlinks) {
    var publications = {};
    var citations = {};
    var collaborations = {};

    var hasPublications = false;
    var hasCitations = false;
    var hasCollaborations = false;

    graphlinks.forEach(function(link) {
        var toModify = null;
        switch (link.type) {
            case "publication":
            case "authorship":
                toModify = publications;
                hasPublications = true;
                break;
            case "citation":
                toModify = citations;
                hasCitations = true;
                break;
            case "collaboration":
                toModify = collaborations;
                hasCollaborations = true;
                break;
            default:
                break;
        }
        if(toModify !== null) {
            if(toModify[link.source.id] === undefined) {
                toModify[link.source.id] = [];
            }
            if(toModify[link.target.id] === undefined) {
                toModify[link.target.id] = [];
            }

            if(toModify[link.source.id].indexOf(link) === -1) {
                toModify[link.source.id].push(link);
            }
            if(toModify[link.target.id].indexOf(link) === -1) {
                toModify[link.target.id].push(link);
            }
        }
    });
    return {publications: publications, citations: citations, collaborations: collaborations, hasPublications: hasPublications, hasCitations: hasCitations, hasCollaborations: hasCollaborations};
}

function tick() {
    d3.selectAll(".link")
        .attr("d", linkArc)
        .attr("d", linkArcRadius);

    d3.selectAll(".node")
        .attr("transform", transform);

    d3.selectAll(".node-text")
        .attr("transform", function(d) {
            var bbox = this.getBBox();
            return "translate(" + (d.x - bbox.width / 2.0) + "," + (d.y + d.size + 8) + ")";
        });
}

function linkArc(d) {
    // var dx = d.target.x - d.source.x,
    //     dy = d.target.y - d.source.y,
        // dr = 2 * Math.sqrt(dx * dx + dy * dy);
    // + dr + "," + dr + " 0 0,1 "
    return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
}

function linkArcRadius(d) {
    // recalculate and back off the distance
    // length of current path
    var pl = this.getTotalLength(),
        // radius of circle plus marker head
        r = (d.target.size) + 6, //16.97 is the "size" of the marker Math.sqrt(12**2 + 12 **2)
        r2 = d.doubled ? (d.source.size) + 6 : (d.source.size),
        // position close to where path intercepts circle
        m = this.getPointAtLength(pl - r),
        m2 = this.getPointAtLength(r2);
    // var dx = m.x - m2.x,
        // dy = m.y - m2.y,
        // dr = 2 * Math.sqrt(dx * dx + dy * dy);
    // return "M" + m2.x + "," + m2.y + "A" + dr + "," + dr + " 0 0,1 " + m.x + "," + m.y;
    return "M" + m2.x + "," + m2.y + "L" + m.x + "," + m.y;
}

function transform(d) {
    return "translate(" + d.x + "," + d.y + ")";
}

function updateLayout() {
    force.links(links);
    force.nodes(nodes);
    links = force.links();
    nodes = force.nodes();
    current_graph = buildGraph(links);
    restart();
}

function restart() {
    console.log(links);
    console.log(nodes);

    // links
    var link = root.selectAll(".link")
        .data(links, function(d) {return d.source.id + "_" + d.target.id});
    link.exit()
        .remove();
    link.enter()
        .insert("path", ".node")
        .attr("class", function(d) { return "link " + d.type; })
        .attr("d", linkArc)
        .attr("d", linkArcRadius)
        .attr("class", function(d) { return "link " + d.type; })
        .attr("marker-end", function(d) { return "url(#m" + d.type + ")"; })
        .attr("marker-start", function(d) {return d.doubled ? "url(#m" + d.type + ")" : ""})
        .attr("id", function (d, i) {return 'edgepath' + i})
        .attr("stroke-width", function(d) { return d.size; });

    // node
    var node = root.selectAll(".node")
        .data(nodes, function(d) {return d.id});
    node.exit()
        .remove();
    node.enter()
        .insert("a", ".node-text")
        .attr("class", "node")
        .on("click", handleControls)
        .call(node_drag)
        .append("svg:circle")
        .attr("class", "nodecircle")
        .append("title")
        .attr("class", "nodetitle");

    d3.selectAll(".nodecircle")
        .attr("id", function(d) { return d.id;})
        .attr("r", function(d) { return d.size; })
        .attr("fill", function(d) { return colors[d.group]; });

    d3.selectAll(".nodetitle")
        .text(function(d) { return d.realgroup === 2 ? d.description : ""});

    // node text
    var nodetext = root.selectAll(".node-text")
        .data(nodes, function(d) {return d.id});
    nodetext.exit()
        .remove();
    nodetext.enter().append("svg:text")
        .attr("class", "node-text");

    d3.selectAll(".node-text")
        .text(function(d) {return d.realgroup == 2 ? d.id : d.description});

    force.start();
}

/*
----------------- MAIN -----------------
 */

// INITIALIZATION
var edges = 50;

var container = $('.chart-container'),
    width = container.width(),
    height = container.height();
var colors = initColors();
initMarkers();
var data = initData();
var canvas = initSvg(width, height);
var force = initForceLayout([], [], width, height);
var svg = canvas.svg,
    root = canvas.root;
initZoomBehahviour(svg);
var node_drag = initDragBehaviour(force);

// build initial state
// add links
var init_links = data.all_links.slice(0, Math.min(edges, data.all_links.length));
var links = [];
//add Nodes
var nodes = [];
var current_graph;
init_links.forEach(function(link) {
    if(nodes.indexOf(link.source) === -1 ) {
        console.log("SOURCE NODE DOES NOT EXIST!!!");
        nodes.push(link.source);
        addAllCurrentLinks(link.source);
    }
    if(nodes.indexOf(link.target) === -1 ) {
        console.log("TARGET NODE DOES NOT EXIST!!!");
        nodes.push(link.target);
        addAllCurrentLinks(link.target);
    }
});

updateLayout();

function addLinks(linksToAdd) {
    // add all links
    linksToAdd.forEach(function(link) {
        var linkIndex = links.indexOf(link);
        if(linkIndex === -1) {

            var addedLink = false;
            // maybe add Nodes
            if(nodes.indexOf(link.source) === -1 ) {
                console.log("SOURCE NODE DOES NOT EXIST!!!");
                nodes.push(link.source);
                addAllCurrentLinks(link.source);
                addedLink = true;
            }
            if(nodes.indexOf(link.target) === -1 ) {
                console.log("TARGET NODE DOES NOT EXIST!!!");
                nodes.push(link.target);
                addAllCurrentLinks(link.target);
                addedLink = true
            }
            if(addedLink === false) {
                // add link
                links.push(link);
                // update link count of affected links
                nodes[nodes.indexOf(link.target)].links += 1;
                nodes[nodes.indexOf(link.source)].links += 1;
            }
        }
    });
    updateLayout();
}

function removeLinks(linksToRemove) {

    // remove all links
    linksToRemove.forEach(function(link) {
        var linkIndex = links.indexOf(link);
        if(linkIndex !== -1) {

            // remove link
            var removedLink = links.splice(linkIndex, 1)[0];

            // update link count of affected nodes
            nodes[nodes.indexOf(removedLink.target)].links -= 1;
            nodes[nodes.indexOf(removedLink.source)].links -= 1;
        }
    });

    // filter nodes if links = 0
    nodes = nodes.filter(function(node) {
        return node.links > 0;
    });
    updateLayout();
}

function search() {

    console.log(document.getElementById('search').value);
    var patt = new RegExp(".*"+document.getElementById('search').value+".*");

    nodes.forEach(function(node) {
        node.group = data.all_nodes[node.id].realgroup;

        var description = node.description.toLowerCase();
        if(patt.test(description)) {
            node.group = 3;
        }
    });

    updateLayout();
}

function addAllCurrentLinks(newnode) {
    if(data.all_graph.hasPublications && data.all_graph.publications[newnode.id] !== undefined)
        data.all_graph.publications[newnode.id].forEach(function(alllink) {subAddAll(newnode, alllink);});

    if(newnode.realgroup === 2 && data.all_graph.hasCitations && data.all_graph.citations[newnode.id] !== undefined) {
        data.all_graph.citations[newnode.id].forEach(function(alllink) {subAddAll(newnode, alllink);});
    } else if (newnode.realgroup === 1 && data.all_graph.hasCollaborations && data.all_graph.collaborations[newnode.id] !== undefined) {
        data.all_graph.collaborations[newnode.id].forEach(function(alllink) {subAddAll(newnode, alllink);});
    }
}

function subAddAll(newnode, alllink) {
    var t = nodes.indexOf(alllink.target);
    var s = nodes.indexOf(alllink.source);

    if((alllink.source === newnode && t !== -1) ||
        (alllink.target === newnode && s !== -1)) {
        // add link
        links.push(alllink);

        // update link count of affected links
        nodes[t].links += 1;
        nodes[s].links += 1;
    }
}