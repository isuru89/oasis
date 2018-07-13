import React, { Component } from 'react'
import styled from 'styled-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

const Wrapper = styled.div`
  padding: 5px;
  margin: 2px;
`

const Expander = styled.span`
  color: #739CAE;
  opacity: 0.5;
  cursor: pointer;
  min-width: 30px;
  text-align: center;

  &:hover {
    opacity: 1;
  }
`

const Title = styled.span`
  flex: 1;
`

const TitleBar = styled.div`
  padding: 5px;
  padding-bottom: 4px;
  border: 2px solid #739CAE;
  border-radius: 5px 5px 0 0;
  display: flex;
`

const Content = styled.div`
  padding: 5px;
  border-radius: 0 0 5px 5px;
  border: 1px solid #739CAE;
  border-top: none;
`

export default class Panel extends Component {

  state = {
    expanded: true
  }

  render() {
    const { expanded } = this.state;

    return (
      <Wrapper>
        <TitleBar>
          <Title>{this.props.title}</Title>
          <Expander onClick={this._whenExpandClicked}>
            {
              expanded ? <FontAwesomeIcon icon="caret-down" /> : <FontAwesomeIcon icon="caret-right" />
            }
          </Expander>
        </TitleBar>
        <Content>
          {expanded && this.props.children}
        </Content>
      </Wrapper>
    )
  }

  _whenExpandClicked = e => this.setState({ expanded: !this.state.expanded });
}
